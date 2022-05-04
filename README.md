# health-apis-datamart-synthetic-records

Synthetic data for [health-apis-data-query](https://github.com/department-of-veterans-affairs/health-apis-data-query)
for use in local development and test environments

See also
[Sandbox Test Accounts for Health APIs](https://github.com/department-of-veterans-affairs/vets-api-clients/blob/master/test_accounts/health_test_accounts.md)

### Local Development

1. Docker is required
2. Download [Flyway](https://flywaydb.org/download/) and unzip it in the root of this repository
3. Use `./run-local.sh clean` to create a new database (as a docker image) and populate it

To repopulate an existing database, retaining existing schema and records, use `./run-local.sh`
with no arguments

To limit the resources updated, use `RESOURCES` environment variable with comma-separated resource classes

```
$ RESOURCES=Condition,Location run-local.sh
...
11:50:25 [INFO] Running gov.va.api.health.minimartmanager.PopulateDb
[main] g.v.a.health.minimartmanager.PopulateDb : No patient specifed, defaulting to all patients.
[main] g.v.a.health.minimartmanager.PopulateDb : Overriding default resources.
[main] g.v.a.health.minimartmanager.PopulateDb : Only synchronizing [Condition, Location]
...
```
