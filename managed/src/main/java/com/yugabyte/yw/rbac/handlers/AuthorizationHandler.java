// Copyright (c) Yugabyte, Inc.
package com.yugabyte.yw.rbac.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.yugabyte.yw.common.config.RuntimeConfigFactory;
import com.yugabyte.yw.controllers.JWTVerifier;
import com.yugabyte.yw.controllers.RequestContext;
import com.yugabyte.yw.controllers.TokenAuthenticator;
import com.yugabyte.yw.models.Customer;
import com.yugabyte.yw.models.Users;
import com.yugabyte.yw.models.rbac.ResourceGroup;
import com.yugabyte.yw.models.rbac.RoleBinding;
import com.yugabyte.yw.rbac.annotations.AuthzPath;
import com.yugabyte.yw.rbac.annotations.PermissionAttribute;
import com.yugabyte.yw.rbac.annotations.RequiredPermissionOnResource;
import com.yugabyte.yw.rbac.annotations.Resource;
import io.ebean.Finder;
import io.ebean.Model;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.pac4j.play.store.PlaySessionStore;
import play.libs.typedmap.TypedKey;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

@Slf4j
public class AuthorizationHandler extends Action<AuthzPath> {

  public static final String COOKIE_AUTH_TOKEN = "authToken";
  public static final String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";
  public static final String COOKIE_API_TOKEN = "apiToken";
  public static final String API_TOKEN_HEADER = "X-AUTH-YW-API-TOKEN";
  public static final String API_JWT_HEADER = "X-AUTH-YW-API-JWT";
  public static final String COOKIE_PLAY_SESSION = "PLAY_SESSION";
  private static final String CUSTOMERS = "customers";

  private final Config config;
  private final RuntimeConfigFactory runtimeConfigFactory;
  private final PlaySessionStore sessionStore;
  private final JWTVerifier jwtVerifier;
  private final TokenAuthenticator tokenAuthenticator;

  private static final String UUID_PATTERN =
      "([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})(/.*)?";
  public static final TypedKey<Customer> CUSTOMER = TypedKey.create("customer");
  public static final TypedKey<Users> USER = TypedKey.create("user");

  @Inject
  public AuthorizationHandler(
      Config config,
      PlaySessionStore sessionStore,
      RuntimeConfigFactory runtimeConfigFactory,
      JWTVerifier jwtVerifier,
      TokenAuthenticator tokenAuthenticator) {
    this.config = config;
    this.sessionStore = sessionStore;
    this.runtimeConfigFactory = runtimeConfigFactory;
    this.jwtVerifier = jwtVerifier;
    this.tokenAuthenticator = tokenAuthenticator;
  }

  @Override
  public CompletionStage<Result> call(Http.Request request) {
    boolean useNewAuthz =
        runtimeConfigFactory.globalRuntimeConf().getBoolean("yb.rbac.use_new_authz");
    if (!useNewAuthz) {
      return delegate.call(request);
    }
    Users user = tokenAuthenticator.getCurrentAuthenticatedUser(request);
    if (user == null) {
      return CompletableFuture.completedFuture(Results.unauthorized("Unable To authenticate User"));
    }

    String endpoint = request.path();
    UUID customerUUID = null;
    Pattern custPattern = Pattern.compile(String.format(".*/%s/" + UUID_PATTERN, CUSTOMERS));
    Matcher custMatcher = custPattern.matcher(endpoint);
    if (custMatcher.find()) {
      customerUUID = UUID.fromString(custMatcher.group(1));
    }

    if (customerUUID != null && !user.getCustomerUUID().equals(customerUUID)) {
      return CompletableFuture.completedFuture(Results.unauthorized("Unable To authenticate User"));
    }

    RequiredPermissionOnResource[] permissionPathList = configuration.value();

    boolean state = true;
    List<RoleBinding> roleBindings = RoleBinding.fetchRoleBindingsForUser(user.getUuid());
    for (RequiredPermissionOnResource permissionPath : permissionPathList) {
      PermissionAttribute attribute = permissionPath.requiredPermission();
      Resource resource = permissionPath.resourceLocation();

      List<RoleBinding> applicableRoleBindings =
          roleBindings.stream()
              .filter(
                  r -> {
                    return r.getRole().getPermissionDetails().getPermissionList().stream()
                        .anyMatch(
                            p ->
                                (p.getAction().equals(attribute.action())
                                    && p.getResourceType().equals(attribute.resourceType())));
                  })
              .collect(Collectors.toList());

      if (applicableRoleBindings.isEmpty()) {
        return CompletableFuture.completedFuture(Results.unauthorized("Unable to authorize user"));
      }

      UUID resourceUUID = null;
      boolean isPermissionPresentOnResource;

      switch (resource.sourceType()) {
          // Identify the resourceUUID from the API endpoint. A typical endpoint follows the format
          // http://<platform_endpoint>/api/v1/<other_details>/<resource_identifier>/<resource_uuid>
          // or
          // http://<platform_endpoint>/api/v1/<other_details>?<resource_identifier>=<resource_uuid>
          // If the UUID can not be identified we check for the required permission of a resource
          // type by
          // using allowAll=true
        case ENDPOINT:
          {
            Pattern pattern =
                Pattern.compile(
                    String.format("(.*\\/|\\?)%s(\\/|=)" + UUID_PATTERN, resource.path()));
            Matcher matcher = pattern.matcher(endpoint);
            if (matcher.find()) {
              resourceUUID = UUID.fromString(matcher.group(3));
            } else if (resource.path().equals(CUSTOMERS)) {
              resourceUUID = user.getCustomerUUID();
            }
            isPermissionPresentOnResource =
                checkResourcePermission(applicableRoleBindings, attribute, resourceUUID);
            if (!isPermissionPresentOnResource) {
              return CompletableFuture.completedFuture(
                  Results.unauthorized("Unable to authorize user"));
            }
            break;
          }
        case REQUEST_BODY:
          {
            JsonNode requestBody = request.body().asJson();
            String[] pathList = resource.path().split("\\.");
            for (String path : pathList) {
              requestBody = requestBody.get(path);
            }
            try {
              resourceUUID = UUID.fromString(requestBody.asText());
            } catch (Exception ex) {
              resourceUUID = null;
            }

            isPermissionPresentOnResource =
                checkResourcePermission(applicableRoleBindings, attribute, resourceUUID);
            if (!isPermissionPresentOnResource) {
              return CompletableFuture.completedFuture(
                  Results.unauthorized("Unable to authorize user"));
            }
            break;
          }
        case DB:
          {
            Pattern pattern =
                Pattern.compile(
                    String.format("(.*\\/|\\?)%s(\\/|=)" + UUID_PATTERN, resource.identifier()));
            Matcher matcher = pattern.matcher(request.path());
            if (matcher.find()) {
              resourceUUID = UUID.fromString(matcher.group(3));
            }
            Class<? extends Model> modelClass = resource.dbClass();
            Finder<UUID, Model> find = new Finder(modelClass);

            Model modelEntity =
                find.query().where().eq(resource.columnName(), resourceUUID).findOne();
            ObjectMapper mapper = new ObjectMapper();
            if (modelEntity == null) {
              return CompletableFuture.completedFuture(
                  Results.unauthorized("Unable to authorize user"));
            }
            JsonNode requestBody = mapper.convertValue(modelEntity, JsonNode.class);

            String[] pathList = resource.path().split("\\.");
            for (String path : pathList) {
              requestBody = requestBody.get(path);
            }

            try {
              resourceUUID = UUID.fromString(requestBody.asText());
            } catch (Exception ex) {
              resourceUUID = null;
            }

            isPermissionPresentOnResource =
                checkResourcePermission(applicableRoleBindings, attribute, resourceUUID);
            if (!isPermissionPresentOnResource) {
              return CompletableFuture.completedFuture(
                  Results.unauthorized("Unable to authorize user"));
            }
            break;
          }
        default:
          {
            return CompletableFuture.completedFuture(
                Results.unauthorized("Unable to authorize user"));
          }
      }
    }
    RequestContext.put(CUSTOMER, Customer.get(user.getCustomerUUID()));
    RequestContext.put(USER, user);
    return delegate.call(request);
  }

  private boolean checkResourcePermission(
      List<RoleBinding> roleBindings, PermissionAttribute attribute, UUID resourceId) {
    return roleBindings.stream()
        .anyMatch(
            r -> {
              for (ResourceGroup.ResourceDefinition rD :
                  r.getResourceGroup().getResourceDefinitionSet()) {
                if (rD.getResourceType().equals(attribute.resourceType())) {
                  if (rD.isAllowAll()) {
                    return true;
                  } else if (resourceId != null && rD.getResourceUUIDSet().contains(resourceId)) {
                    return true;
                  }
                }
              }
              return false;
            });
  }
}
