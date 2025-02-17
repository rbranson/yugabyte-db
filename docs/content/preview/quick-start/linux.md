---
title: YugabyteDB Quick start for Linux
headerTitle: Quick start
linkTitle: Linux
description: Test YugabyteDB's APIs and core features by creating a local cluster on a single host.
headcontent: Create a local cluster on a single host
aliases:
  - /quick-start/linux/
type: docs
rightNav:
  hideH4: true
unversioned: true
---

<ul class="nav nav-tabs-alt nav-tabs-yb">
  <li>
    <a href="../../quick-start-yugabytedb-managed/" class="nav-link">
      <img src="/icons/cloud.svg" alt="Cloud Icon">
      Use a cloud cluster
    </a>
  </li>
  <li class="active">
    <a href="../../quick-start/" class="nav-link">
      <img src="/icons/database.svg" alt="Server Icon">
      Use a local cluster
    </a>
  </li>
</ul>

The local cluster setup on a single host is intended for development and learning. For production deployment, performance benchmarking, or deploying a true multi-node on multi-host setup, see [Deploy YugabyteDB](../../deploy/).

<ul class="nav nav-tabs-alt nav-tabs-yb">
  <li>
    <a href="../" class="nav-link">
      <i class="fa-brands fa-apple" aria-hidden="true"></i>
      macOS
    </a>
  </li>
  <li class="active">
    <a href="../linux/" class="nav-link">
      <i class="fa-brands fa-linux" aria-hidden="true"></i>
      Linux
    </a>
  </li>
  <li>
    <a href="../docker/" class="nav-link">
      <i class="fa-brands fa-docker" aria-hidden="true"></i>
      Docker
    </a>
  </li>
  <li>
    <a href="../kubernetes/" class="nav-link">
      <i class="fa-regular fa-dharmachakra" aria-hidden="true"></i>
      Kubernetes
    </a>
  </li>
</ul>

## Install YugabyteDB

Installing YugabyteDB involves completing [prerequisites](#prerequisites) and [downloading the YugabyteDB package](#download-yugabytedb).

### Prerequisites

{{% readfile "include-prerequisites-linux.md" %}}

#### ulimits

Because each tablet maps to its own file, you can create a very large number of files in the current shell by experimenting with several hundred tables and several tablets per table. You need to [configure ulimit values](../../deploy/manual-deployment/system-config/#ulimits).

### Download YugabyteDB

YugabyteDB supports both x86 and ARM (aarch64) CPU architectures. Download packages ending in `x86_64.tar.gz` to run on x86, and packages ending in `aarch64.tar.gz` to run on ARM.

The following instructions are for downloading the Preview release of YugabyteDB, which is recommended for development and testing only. For other versions, see [Releases](../../releases/).

Download YugabyteDB as follows:

1. Download the YugabyteDB package using one of the following `wget` commands:

    ```sh
    wget https://downloads.yugabyte.com/releases/{{< yb-version version="preview">}}/yugabyte-{{< yb-version version="preview" format="build">}}-linux-x86_64.tar.gz
    ```

    Or:

    ```sh
    wget https://downloads.yugabyte.com/releases/{{< yb-version version="preview">}}/yugabyte-{{< yb-version version="preview" format="build">}}-el8-aarch64.tar.gz
    ```

1. Extract the package and then change directories to the YugabyteDB home.

    ```sh
    tar xvfz yugabyte-{{< yb-version version="preview" format="build">}}-linux-x86_64.tar.gz && cd yugabyte-{{< yb-version version="preview">}}/
    ```

    Or:

    ```sh
    tar xvfz yugabyte-{{< yb-version version="preview" format="build">}}-el8-aarch64.tar.gz && cd yugabyte-{{< yb-version version="preview">}}/
    ```

### Configure YugabyteDB

To configure YugabyteDB, run the following shell script:

```sh
./bin/post_install.sh
```

## Create a local cluster

To create a single-node local cluster with a replication factor (RF) of 1, run the following command:

```sh
./bin/yugabyted start
```

After the cluster has been created, clients can connect to the YSQL and YCQL APIs at `http://localhost:5433` and `http://localhost:9042` respectively. You can also check `~/var/data` to see the data directory and `~/var/logs` to see the logs directory.

If you have previously installed YugabyteDB 2.8 or later and created a cluster on the same computer, you may need to [upgrade the YSQL system catalog](../../manage/upgrade-deployment/#upgrade-the-ysql-system-catalog) to run the latest features.

### Check the cluster status

Execute the following command to check the cluster status:

```sh
./bin/yugabyted status
```

Expect an output similar to the following:

```output
+--------------------------------------------------------------------------------------------------+
|                                            yugabyted                                             |
+--------------------------------------------------------------------------------------------------+
| Status              : Running. Leader Master is present                                          |
| Web console         : http://127.0.0.1:7000                                                      |
| JDBC                : jdbc:postgresql://127.0.0.1:5433/yugabyte?user=yugabyte&password=yugabyte  |
| YSQL                : bin/ysqlsh   -U yugabyte -d yugabyte                                       |
| YCQL                : bin/ycqlsh   -u cassandra                                                  |
| Data Dir            : /Users/myuser/var/data                                                     |
| Log Dir             : /Users/myuser/var/logs                                                     |
| Universe UUID       : fad6c687-e1dc-4dfd-af4b-380021e19be3                                       |
+--------------------------------------------------------------------------------------------------+
```

### Use the Admin UI

The cluster you have created consists of two processes: [YB-Master](../../architecture/concepts/yb-master/) which keeps track of various metadata (list of tables, users, roles, permissions, and so on) and [YB-TServer](../../architecture/concepts/yb-tserver/) which is responsible for the actual end-user requests for data updates and queries.

Each of these processes exposes its own Admin UI that you can use to check the status of the corresponding process, and perform certain administrative operations. The UIs are available via your local machine's IP address, as displayed in the **Web console** output of the `status` command you ran.

The [YB-Master Admin UI](../../reference/configuration/yb-master/#admin-ui) uses port 7000 and is typically at <http://127.0.0.1:7000>, while the [YB-TServer Admin UI](../../reference/configuration/yb-tserver/#admin-ui) uses port 9000 and is typically at <http://127.0.0.1:9000>.

#### Overview and YB-Master status

The following illustration shows the YB-Master home page with a cluster with a replication factor of 1, a single node, and no tables. The YugabyteDB version is also displayed.

![master-home](/images/admin/master-home-binary-rf1.png)

The **Masters** section highlights the 1 YB-Master along with its corresponding cloud, region, and zone placement.

#### YB-TServer status

Click **See all nodes** to open the **Tablet Servers** page that lists the YB-TServer along with the time since it last connected to the YB-Master using regular heartbeats. Because there are no user tables, **User Tablet-Peers / Leaders** is 0. As tables are added, new tablets (also known as shards) will be created automatically and distributed evenly across all the available tablet servers.

![master-home](/images/admin/master-tservers-list-binary-rf1.png)

## Connect to the database

Using the YugabyteDB SQL shell, [ysqlsh](../../admin/ysqlsh/), you can connect to your cluster and interact with it using distributed SQL. ysqlsh is installed with YugabyteDB and is located in the bin directory of the YugabyteDB home directory.

To open the YSQL shell, run `ysqlsh`.

```sh
$ ./bin/ysqlsh
```

```output
ysqlsh (11.2-YB-2.1.0.0-b0)
Type "help" for help.

yugabyte=#
```

To load sample data and explore an example using ysqlsh, refer to [Retail Analytics](../../sample-data/retail-analytics/).

## Build an application

Applications connect to and interact with YugabyteDB using API client libraries (also known as client drivers). This section shows how to connect applications to your cluster using your favorite programming language.

### Choose your language

{{< readfile "/preview/quick-start-yugabytedb-managed/quick-start-buildapps-include.md" >}}

## Next step

[Explore YugabyteDB](../../explore/)
