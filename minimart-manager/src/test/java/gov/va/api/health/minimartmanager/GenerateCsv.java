package gov.va.api.health.minimartmanager;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.allergyintolerance.DatamartAllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.health.minimartmanager.minimart.DatamartFilenamePatterns;
import gov.va.api.health.minimartmanager.minimart.MakerUtils;
import gov.va.api.lighthouse.datamart.DatamartCoding;
import gov.va.api.lighthouse.datamart.HasReplaceableId;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.Test;

@Slf4j
public class GenerateCsv {
  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  private static final Map<String, DatamartPatient> PATIENT_MAP = loadPatients();

  private static final String CSV_NAME = "health-test-patient-data.csv";

  private static final String[] CSV_HEADERS = {
    "PatientEmail",
    "PatientIcn",
    "Name",
    "Birthdate",
    "Resource",
    "CodeSystem",
    "Code",
    "Description",
    "Status",
    "Classification",
    "Date"
  };

  /** Resources to generate csv entries. To be filled out over time */
  private static final List<String> RESOURCES = List.of("AllergyIntolerance");

  private final Function<DatamartAllergyIntolerance, List<String>> toAllergyIntoleranceCsv =
      datamartAllergyIntolerance -> {
        List<String> csvOutput = new ArrayList<>(11);
        csvOutput.add("PatientEmail");
        // Use the patient map to find the patient's name and birthdate by ICN
        var icn = datamartAllergyIntolerance.patient().reference().orElse("");
        var dmPatient = PATIENT_MAP.get(icn);
        checkNotNull(dmPatient);
        csvOutput.add(icn);
        csvOutput.add(dmPatient.name());
        csvOutput.add(dmPatient.birthDateTime().substring(0, 10));
        csvOutput.add("AllergyIntolerance");
        csvOutput.add(
            datamartAllergyIntolerance
                .substance()
                .flatMap(DatamartAllergyIntolerance.Substance::coding)
                .flatMap(DatamartCoding::system)
                .orElse(""));
        csvOutput.add(
            datamartAllergyIntolerance
                .substance()
                .flatMap(DatamartAllergyIntolerance.Substance::coding)
                .flatMap(DatamartCoding::code)
                .orElse(""));
        csvOutput.add(
            datamartAllergyIntolerance
                .substance()
                .flatMap(DatamartAllergyIntolerance.Substance::coding)
                .flatMap(DatamartCoding::display)
                .orElse(""));
        csvOutput.add(datamartAllergyIntolerance.clinicalStatus().name());
        csvOutput.add("");
        csvOutput.add(
            Objects.requireNonNull(datamartAllergyIntolerance.recordedDate().orElse(null))
                .toString());
        return csvOutput;
      };

  static File importDirectory() {
    String importDirectoryPath = System.getProperty("import.directory");
    if (isBlank(importDirectoryPath)) {
      throw new IllegalStateException("import.directory not specified");
    }
    return new File(importDirectoryPath);
  }

  static Map<String, DatamartPatient> loadPatients() {
    var map = new HashMap<String, DatamartPatient>();
    MakerUtils.findUniqueFiles(
            importDirectory(), DatamartFilenamePatterns.get().json(DatamartPatient.class))
        .map(f -> MakerUtils.fileToDatamart(MAPPER, f, DatamartPatient.class))
        .forEach(dmPatient -> map.put(dmPatient.fullIcn(), dmPatient));
    return map;
  }

  @Test
  @SneakyThrows
  void createCsv() {
    try (CSVPrinter printer =
        new CSVPrinter(new FileWriter(CSV_NAME), CSVFormat.DEFAULT.withHeader(CSV_HEADERS))) {
      // per resource, add the datamart records found in the import directory to the csv file.
      var resourcesToUpdate = RESOURCES;
      var userSpecifiedResources = System.getProperty("resources");
      if (isNotBlank(userSpecifiedResources)) {
        resourcesToUpdate = Arrays.asList(userSpecifiedResources.split(",", -1));
        log.warn("Overriding default resources. Only summarizing {}", resourcesToUpdate);
      }
      var importDirectory = importDirectory();
      for (String resource : resourcesToUpdate) {
        log.info(
            "Adding to csv file with RESOURCE: {}, IMPORT DIRECTORY: {}",
            resource,
            importDirectory);
        for (var record : toCsvRecords(importDirectory, resource)) {
          printer.printRecord(record);
        }
      }
    }
  }

  List<List<String>> toCsvRecords(File dir, String resource) {
    switch (resource) {
      case "AllergyIntolerance":
        return toCsvRecords(dir, DatamartAllergyIntolerance.class, toAllergyIntoleranceCsv);
      default:
        throw new RuntimeException("Unsupported resource type: " + resource);
    }
  }

  <DM extends HasReplaceableId> List<List<String>> toCsvRecords(
      File directory, Class<DM> resourceType, Function<DM, List<String>> toDatamartCsv) {
    return MakerUtils.findUniqueFiles(directory, DatamartFilenamePatterns.get().json(resourceType))
        .sorted()
        .parallel()
        .map(f -> toDatamartCsv.apply(MakerUtils.fileToDatamart(MAPPER, f, resourceType)))
        .collect(toList());
  }
}
