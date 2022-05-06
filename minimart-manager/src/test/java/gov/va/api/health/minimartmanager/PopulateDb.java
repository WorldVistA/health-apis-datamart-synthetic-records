package gov.va.api.health.minimartmanager;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Populate a database with datamart style data. Pull in the datamart entity models from Data-Query,
 * etc...
 *
 * <p>The push to database is now integrated into the maven test lifecycle. This push requires a few
 * system properties to be provided:
 *
 * <p>(REQUIRED) DIRECTORY_TO_IMPORT : path to the datamart data to be loaded.
 *
 * <p>(OPTIONAL) PATIENT : restrict db load to a specific patient; defaults to all.
 *
 * <p>(REQUIRED) CONFIG_FILE : database connection configuration.
 *
 * <p>The synthetic-data jenkins job will automatically set and launch this test during database
 * population.
 */
@Slf4j
public class PopulateDb {
  /** Resources to load. */
  private static List<String> resources =
      List.of(
          "AllergyIntolerance",
          "Appointment",
          "Condition",
          "Device",
          "DeviceRequest",
          "DiagnosticReport",
          "Encounter",
          "Immunization",
          "Location",
          "Medication",
          "MedicationOrder",
          "MedicationStatement",
          "Observation",
          "Organization",
          "Patient",
          "Practitioner",
          "PractitionerRole",
          "PractitionerRoleSpecialtyMap",
          "Procedure",
          "VitalVuidMapping");

  private String importDirectoryPath;

  private String configFilePath;

  /** Time to rock'n'roll. */
  @Test
  void pushToDb() {
    // per resource, push the datamart records found in the import directory to the database.
    var resourcesToUpdate = resources;
    var userSpecifiedResources = System.getProperty("resources");
    if (isNotBlank(userSpecifiedResources)) {
      resourcesToUpdate = Arrays.asList(userSpecifiedResources.split(",", -1));
      log.warn("Overriding default resources.");
      log.warn("Only synchronizing {}", resourcesToUpdate);
    }

    MinimartMaker.removeOldEntities(
        configFilePath,
        List.of(
            MinimartRowRemover.removeBloodPressure552844LoincCode(),
            MinimartRowRemover.removeOxygenSaturation594085LoincCode()));

    for (String resource : resourcesToUpdate) {
      log.info(
          "Pushing to database with RESOURCE: {}, IMPORT DIRECTORY: {}, AND CONFIG FILE: {}",
          resource,
          importDirectoryPath,
          configFilePath);
      MinimartMaker.sync(importDirectoryPath, resource, configFilePath);
    }
    log.info("DONE");
  }

  /** Lets do some input validation before attempting a push to the db. */
  @BeforeEach
  public void setup() {
    // Load the import data directory. If not provided, fail.
    importDirectoryPath = System.getProperty("import.directory");
    if (isBlank(importDirectoryPath)) {
      throw new IllegalArgumentException("import.directory not specified");
    }
    // If targeting a patient, use their import data sub-directory.
    // Otherwise, default to all patients.
    String chosenPatient = System.getProperty("patient");
    if (isNotBlank(chosenPatient)) {
      importDirectoryPath = importDirectoryPath + "/dm-records-" + chosenPatient;
    } else {
      log.info("No patient specifed, defaulting to all patients.");
    }
    // Load the config file path. If not provided, fail.
    configFilePath = System.getProperty("config.file");
    if (isBlank(configFilePath)) {
      throw new IllegalArgumentException("CONFIG_FILE not specified.");
    }
  }
}
