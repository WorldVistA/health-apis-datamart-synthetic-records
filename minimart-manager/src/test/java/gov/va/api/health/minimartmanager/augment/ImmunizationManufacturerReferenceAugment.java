package gov.va.api.health.minimartmanager.augment;

import static com.google.common.base.Preconditions.checkState;

import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class ImmunizationManufacturerReferenceAugment {
  private static final Map<String, String> MANUFACTURERS = initManufacturers();

  static DatamartImmunization addManufacturerReferences(
      Augmentation.Context<DatamartImmunization> ctx) {
    DatamartImmunization dm = ctx.resource();
    // The first vaccine code should represent a cvx code.
    // Use this to add the manufacturer reference
    checkState(!dm.vaccineCode().coding().isEmpty());
    var code = dm.vaccineCode().coding().get(0).code().get();

    if (MANUFACTURERS.get(code) != null) {
      dm.manufacturer(
          Optional.of(
              DatamartReference.builder().display(Optional.of(MANUFACTURERS.get(code))).build()));
    }
    return dm;
  }

  @SneakyThrows
  static Map<String, String> initManufacturers() {
    var csvFile =
        ImmunizationManufacturerReferenceAugment.class.getResourceAsStream(
            "cvx-and-manufacturer.csv");
    InputStreamReader reader = new InputStreamReader(csvFile);
    Iterable<CSVRecord> rows =
        CSVFormat.DEFAULT.withDelimiter('|').withFirstRecordAsHeader().parse(reader);
    Map<String, String> cvxManufacturerMap = new HashMap<>();
    for (CSVRecord row : rows) {
      checkState(row.size() == 2);
      cvxManufacturerMap.put(row.get(0), row.get(1));
    }
    return cvxManufacturerMap;
  }

  public static void main(String[] args) {
    Augmentation.forResources(DatamartImmunization.class)
        .whenMatching(Objects::nonNull)
        .transform(ImmunizationManufacturerReferenceAugment::addManufacturerReferences)
        .build()
        .rewriteFiles();
  }
}
