# minimart-manager

This application manages the Minimart database.

## Augments

`Augment` classes are recommended for managing changes across `../datamart/*.json` files

Example of rebuilding `minimart-manager` and executing an augment:

```
mvn clean install -q -P'!standard' && \
mvn exec:java -Dexec.mainClass="gov.va.api.health.minimartmanager.augment.AllergyIntoleranceSubstanceAugment" \
-Dexec.classpathScope="test" -Dexec.cleanupDaemonThreads=false
```

When adding new files, execute `IdValidationAugment` on the applicable subdirectories;
this will reformat the files and validate that the new resource IDs are consistent:

```
mvn exec:java -Dexec.mainClass="gov.va.api.health.minimartmanager.augment.IdValidationAugment" \
-Dexec.args="dm-records-1012659372V317896 dm-records-1012667179V787205" \
-Dexec.classpathScope="test" -Dexec.cleanupDaemonThreads=false
```
