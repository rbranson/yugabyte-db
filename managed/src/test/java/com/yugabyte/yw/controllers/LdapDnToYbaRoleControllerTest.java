/*
 * Copyright 2023 YugaByte, Inc. and Contributors
 *
 * Licensed under the Polyform Free Trial License 1.0.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://github.com/YugaByte/yugabyte-db/blob/master/licenses/POLYFORM-FREE-TRIAL-LICENSE-1.0.0.txt
 */

package com.yugabyte.yw.controllers;

import static com.yugabyte.yw.common.AssertHelper.assertBadRequest;
import static com.yugabyte.yw.common.AssertHelper.assertOk;
import static com.yugabyte.yw.common.AssertHelper.assertPlatformException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.contentAsString;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.yugabyte.yw.common.FakeDBApplication;
import com.yugabyte.yw.common.ModelFactory;
import com.yugabyte.yw.forms.LdapDnToYbaRoleData.LdapDnYbaRoleDataPair;
import com.yugabyte.yw.models.Customer;
import com.yugabyte.yw.models.LdapDnToYbaRole;
import com.yugabyte.yw.models.Users;
import com.yugabyte.yw.models.Users.Role;
import java.io.IOException;
import java.util.List;
import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import play.libs.Json;
import play.mvc.Result;

@RunWith(JUnitParamsRunner.class)
public class LdapDnToYbaRoleControllerTest extends FakeDBApplication {

  private Customer defaultCustomer;
  private Users defaultUser;
  private ObjectMapper mapper;

  @Before
  public void setup() {
    defaultCustomer = ModelFactory.testCustomer();
    defaultUser = ModelFactory.testUser(defaultCustomer, Role.SuperAdmin);
    mapper = new ObjectMapper();
  }

  @Test
  public void testList() throws IOException {
    LdapDnToYbaRole lm1 = LdapDnToYbaRole.create(defaultCustomer.getUuid(), "dn1", Role.Admin);
    LdapDnToYbaRole lm2 =
        LdapDnToYbaRole.create(defaultCustomer.getUuid(), "dn2", Role.BackupAdmin);
    LdapDnToYbaRole lm3 = LdapDnToYbaRole.create(defaultCustomer.getUuid(), "dn3", Role.ReadOnly);

    LdapDnYbaRoleDataPair lp1 = new LdapDnYbaRoleDataPair();
    LdapDnYbaRoleDataPair lp2 = new LdapDnYbaRoleDataPair();
    LdapDnYbaRoleDataPair lp3 = new LdapDnYbaRoleDataPair();

    lp1.setDistinguishedName(lm1.distinguishedName);
    lp2.setDistinguishedName(lm2.distinguishedName);
    lp3.setDistinguishedName(lm3.distinguishedName);

    lp1.setYbaRole(lm1.ybaRole);
    lp2.setYbaRole(lm2.ybaRole);
    lp3.setYbaRole(lm3.ybaRole);

    ObjectReader reader = mapper.readerFor(new TypeReference<List<LdapDnYbaRoleDataPair>>() {});
    List<LdapDnYbaRoleDataPair> LdapDnYbaRoleDataPairs =
        reader.readValue(fetchLdapDnToYbaRoles().get("ldapDnToYbaRolePairs"));
    assertEquals(LdapDnYbaRoleDataPairs.size(), 3);
    assertTrue(LdapDnYbaRoleDataPairs.containsAll(ImmutableList.of(lp1, lp2, lp3)));
  }

  @Test
  public void testFailUpdateRoleBadFormat() {
    ObjectNode bodyJson = Json.newObject();

    ObjectNode invalidMapping = Json.newObject();
    invalidMapping.put("ybaRole", "InvalidRole");
    invalidMapping.put("distinguishedName", "dn");
    ArrayNode pairList = mapper.createArrayNode().add(invalidMapping);

    bodyJson.put("ldapDnToYbaRolePairs", pairList);
    assertPlatformException(() -> updateLdapDnToYbaRoles(bodyJson));
  }

  @Test
  public void testSuccessUpdateMappings() {
    String dn = "cn=user,dc=com";
    Role role = Role.Admin;

    LdapDnYbaRoleDataPair lp = new LdapDnYbaRoleDataPair();
    lp.setDistinguishedName(dn);
    lp.setYbaRole(role);
    ObjectNode bodyJson = Json.newObject();
    ArrayNode pairList = mapper.createArrayNode().add(mapper.convertValue(lp, JsonNode.class));

    bodyJson.put("ldapDnToYbaRolePairs", pairList);
    Result r = updateLdapDnToYbaRoles(bodyJson);
    assertOk(r);

    List<LdapDnToYbaRole> updatedMappings = LdapDnToYbaRole.find.query().findList();

    assertEquals(updatedMappings.size(), 1);
    assertEquals(updatedMappings.get(0).distinguishedName, dn);
    assertEquals(updatedMappings.get(0).ybaRole, role);
  }

  @Test
  public void testOverwriteMappings() {
    String dn = "cn=user,dc=com";
    Role role = Role.Admin;
    LdapDnToYbaRole oldMapping = LdapDnToYbaRole.create(defaultCustomer.getUuid(), dn, role);

    String dn2 = "cn=user2,dc=org";
    Role role2 = Role.BackupAdmin;

    LdapDnYbaRoleDataPair lp = new LdapDnYbaRoleDataPair();
    lp.setDistinguishedName(dn2);
    lp.setYbaRole(role2);
    ObjectNode bodyJson = Json.newObject();
    ArrayNode pairList = mapper.createArrayNode().add(mapper.convertValue(lp, JsonNode.class));
    bodyJson.put("ldapDnToYbaRolePairs", pairList);
    Result r = updateLdapDnToYbaRoles(bodyJson);
    assertOk(r);

    List<LdapDnToYbaRole> updatedMappings = LdapDnToYbaRole.find.query().findList();
    assertEquals(updatedMappings.contains(oldMapping), false);
    assertEquals(updatedMappings.size(), 1);
    assertEquals(updatedMappings.get(0).distinguishedName, dn2);
    assertEquals(updatedMappings.get(0).ybaRole, role2);
  }

  @Test
  public void testSuperAdminMapping() {
    String dn = "cn=user,dc=com";
    Role role = Role.SuperAdmin;

    LdapDnYbaRoleDataPair lp = new LdapDnYbaRoleDataPair();
    lp.setDistinguishedName(dn);
    lp.setYbaRole(role);
    ObjectNode bodyJson = Json.newObject();
    ArrayNode pairList = mapper.createArrayNode().add(mapper.convertValue(lp, JsonNode.class));

    bodyJson.put("ldapDnToYbaRolePairs", pairList);
    Result r = assertPlatformException(() -> updateLdapDnToYbaRoles(bodyJson));
    assertBadRequest(r, "SuperAdmin cannot be mapped to a disinguished name!");
  }

  private JsonNode fetchLdapDnToYbaRoles() {
    String authToken = defaultUser.createAuthToken();
    String method = "GET";
    String url = "/api/customers/" + defaultCustomer.getUuid() + "/ldap_mappings";

    Result r = doRequestWithAuthToken(method, url, authToken);
    assertOk(r);
    return Json.parse(contentAsString(r));
  }

  private Result updateLdapDnToYbaRoles(ObjectNode bodyJson) {
    String authToken = defaultUser.createAuthToken();
    String method = "PUT";
    String url = "/api/customers/" + defaultCustomer.getUuid() + "/ldap_mappings";
    return doRequestWithAuthTokenAndBody(method, url, authToken, bodyJson);
  }
}
