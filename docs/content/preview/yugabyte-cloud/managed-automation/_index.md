---
title: Automation in YugabyteDB Managed
headerTitle: Automation
linkTitle: Automation
description: Automation tools for YugabyteDB Managed.
image: /images/section_icons/explore/administer.png
headcontent: Manage YugabyteDB Managed accounts and clusters using automation
menu:
  preview_yugabyte-cloud:
    parent: yugabytedb-managed
    identifier: managed-automation
    weight: 550
type: indexpage
---

Use the following automation tools to manage your YugabyteDB Managed account and clusters:

| Automation | Description |
| :--------- | :---------- |
| [REST API](https://api-docs.yugabyte.com/docs/managed-apis/) | Deploy and manage database clusters using a REST API. |
| [Terraform provider](https://registry.terraform.io/providers/yugabyte/ybm/latest/docs) | Provider for automating YugabyteDB Managed resources that are accessible via the API. |
| [CLI](managed-cli/) | Manage YugabyteDB Managed resources from the command line. |

### Authentication

For access, automation tools require authentication in the form of an [API key](managed-apikeys/).

### Account details

For some REST API commands, you may need one or more of the following account details:

- Account ID.

    To view your account ID, click the **Profile** icon in the top right corner of the YugabyteDB Managed window. The account ID is also displayed in the **API Key Details** sheet.

- Project ID.

    The project ID is a unique identifier of the YugabyteDB Managed project under which database clusters can be deployed. Currently, accounts have only one project. The project ID is also available via the **Profile** icon in the top right corner of the YugabyteDB Managed window. The project ID is also displayed in the **API Key Details** sheet.

- Cluster ID.

    Every cluster has a unique ID. The cluster ID is available via the cluster **Settings** tab.

These identifiers can also be found in the URL when you access a cluster using the YugabyteDB Managed user interface.

<!--div class="row">

  <div class="col-12 col-md-6 col-lg-12 col-xl-6">
    <a class="section-link icon-offset" href="managed-apikeys/">
      <div class="head">
        <img class="icon" src="/images/section_icons/secure/checklist.png" aria-hidden="true" />
        <div class="title">Manage API keys</div>
      </div>
      <div class="body">
        Create and revoke API keys for authenticating access using automation.
      </div>
    </a>
  </div>

</div> -->

### Automation

<div class="row">

  <div class="col-12 col-md-6 col-lg-12 col-xl-6">
    <a class="section-link icon-offset" href="https://api-docs.yugabyte.com/docs/managed-apis/">
      <div class="head">
        <img class="icon" src="/images/section_icons/develop/api-icon.png" aria-hidden="true" />
        <div class="title">YugabyteDB Managed API</div>
      </div>
      <div class="body">
        Manage your account and clusters using a REST API.
      </div>
    </a>
  </div>

  <div class="col-12 col-md-6 col-lg-12 col-xl-6">
    <a class="section-link icon-offset" href="https://registry.terraform.io/providers/yugabyte/ybm/latest">
      <div class="head">
        <img class="icon" src="/images/section_icons/develop/ecosystem/terraform.png" aria-hidden="true" />
        <div class="title">YugabyteDB Managed Terraform Provider</div>
      </div>
      <div class="body">
        Use the Terraform provider to automate tasks.
      </div>
    </a>
  </div>

  <div class="col-12 col-md-6 col-lg-12 col-xl-6">
    <a class="section-link icon-offset" href="managed-cli/">
      <div class="head">
        <img class="icon" src="/images/section_icons/deploy/enterprise/console.png" aria-hidden="true" />
        <div class="title">Command Line Interface</div>
      </div>
      <div class="body">
        Manage your account and clusters from the command line.
      </div>
    </a>
  </div>

</div>
