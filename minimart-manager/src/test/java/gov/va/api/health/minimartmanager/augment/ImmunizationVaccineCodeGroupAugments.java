package gov.va.api.health.minimartmanager.augment;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Iterables;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.lighthouse.datamart.DatamartCoding;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class ImmunizationVaccineCodeGroupAugments {

  private static Map<String, VaccineCodeRow> VACCINE_CODES = initVaccineCodes();

  @SneakyThrows
  static Map<String, VaccineCodeRow> initVaccineCodes() {
    var csvFile =
        ImmunizationVaccineCodeGroupAugments.class.getResourceAsStream(
            "cvx_cdc_cvx_vaccine_group_map_trimmed.csv");
    InputStreamReader reader = new InputStreamReader(csvFile);
    Iterable<CSVRecord> rows = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
    Map<String, VaccineCodeRow> vaccineCodeMap = new HashMap<>();
    for (CSVRecord row : rows) {
      // Assert that there are 7 values present in the CSV, although the final two, VaccineStatus
      // and ROW_NUM, are unused.
      checkState(row.size() == 7);
      vaccineCodeMap.put(
          row.get(0),
          VaccineCodeRow.builder()
              .cvxCode(row.get(0))
              .immunizationName(row.get(1))
              .cvxForVaccineGroup(row.get(2))
              .vaccineGroupName(row.get(3))
              .shortDescription(row.get(4))
              .build());
    }
    return vaccineCodeMap;
  }

  public static void main(String[] args) {
    Augmentation.forResources(DatamartImmunization.class)
        .whenMatching(Objects::nonNull)
        .transform(ImmunizationVaccineCodeGroupAugments::switchVaccineCodeToArray)
        .build()
        .rewriteFiles();
  }

  static DatamartImmunization switchVaccineCodeToArray(
      Augmentation.Context<DatamartImmunization> ctx) {
    checkState(ctx.resource().vaccineCode().coding().size() == 1);
    var currentCode = Iterables.getOnlyElement(ctx.resource().vaccineCode().coding());
    checkState(currentCode.code().isPresent());
    var matchingVaccineCode = VACCINE_CODES.get(currentCode.code().get());
    if (matchingVaccineCode == null) {
      return ctx.resource();
    }
    ctx.resource()
        .vaccineCode()
        .coding(
            List.of(
                DatamartCoding.builder()
                    .code(Optional.of(matchingVaccineCode.cvxCode()))
                    .display(Optional.of(matchingVaccineCode.immunizationName()))
                    .build(),
                DatamartCoding.builder()
                    .code(Optional.of(matchingVaccineCode.cvxForVaccineGroup()))
                    .display(Optional.of(matchingVaccineCode.vaccineGroupName()))
                    .build()))
        .shortDescription(Optional.of(matchingVaccineCode.shortDescription()));
    return ctx.resource();
  }

  @Builder
  @Value
  static class VaccineCodeRow {
    private String cvxCode;
    private String cvxForVaccineGroup;
    private String vaccineGroupName;
    private String immunizationName;
    private String shortDescription;
  }
}
