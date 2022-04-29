# health-apis-datamart-synthetic-records

Synthetic data for [health-apis-data-query](https://github.com/department-of-veterans-affairs/health-apis-data-query)
for use in local development and test environments.

See also
[Sandbox Test Accounts for Health APIs](https://github.com/department-of-veterans-affairs/vets-api-clients/blob/master/test_accounts/health_test_accounts.md)

### Local Development

1. Docker is required
2. Download [Flyway](https://flywaydb.org/download/) and unzip it in the root of this repository
3. Use `./run-local.sh clean` to create a new database (as a docker image) and populate it

To repopulate an existing database, retaining existing schema and records, use `./run-local.sh`
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

### How to Minifier
Augment class to minify datamart subdirectories (squish up the json), by reading in files and writing them back out.
* For given subdirectories, Minifier also verifies that filenames match internally defined IDs
* Minifier verifies globally that there are not duplicate filenames
Example
```
mvn clean install && \
mvn exec:java -Dexec.mainClass="gov.va.api.health.minimartmanager.minimart.augments.Minifier" \
-Dexec.args="dm-records-1012659372V317896 dm-records-1012667179V787205" \
-Dexec.classpathScope="test" -Dexec.cleanupDaemonThreads=false
```