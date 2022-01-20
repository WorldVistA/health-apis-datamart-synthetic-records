# health-apis-datamart-synthetic-records

This repository acts as storage for synthetic data via JSON documents.
It also provides tools for building reproducible (MITRE) databases filled with synthetic data.

### Creating a Local Synthetic Records Database

##### Prerequisites

1. Docker
2. [Flyway](https://flywaydb.org/download/)

##### Launching the Database

1. Download Flyway and unzip it in the root of this repository
2. Use `./run-local.sh clean` to create a new database (as a docker image) and populate it

To populate an existing database, retaining existing schema and records, use `./run-local.sh`
with no arguments.

You can control the resources updated for the local development using `RESOURCES` environment
variable to be a CSV list of resources classes to update.

```
$ RESOURCES=Condition,Location run-local.sh

...
11:50:25 [INFO] Running gov.va.api.health.minimartmanager.PopulateDb
2021-05-10 11:50:26.258  INFO   --- [           main] g.v.a.health.minimartmanager.PopulateDb  : No patient specifed, defaulting to all patients.
2021-05-10 11:50:26.264  WARN   --- [           main] g.v.a.health.minimartmanager.PopulateDb  : Overriding default resources.
2021-05-10 11:50:26.264  WARN   --- [           main] g.v.a.health.minimartmanager.PopulateDb  : Only synchronizing [Condition, Location]
...

```
