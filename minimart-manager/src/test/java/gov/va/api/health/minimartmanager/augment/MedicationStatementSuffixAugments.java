package gov.va.api.health.minimartmanager.augment;

import static com.google.common.base.Preconditions.checkNotNull;

import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import java.util.Objects;

public class MedicationStatementSuffixAugments {

  static DatamartMedicationStatement addMedicationStatementIdSuffixToMs(
      Augmentation.Context<DatamartMedicationStatement> ctx) {
    DatamartMedicationStatement dm = ctx.resource();
    if (!dm.cdwId().endsWith(":M")) {
      dm.cdwId(dm.cdwId() + ":M");
    }
    checkNotNull(CompositeCdwId.fromCdwId(dm.cdwId()));
    return dm;
  }

  public static void main(String[] args) {
    Augmentation.forResources(DatamartMedicationStatement.class)
        .whenMatching(Objects::nonNull)
        .transform(MedicationStatementSuffixAugments::addMedicationStatementIdSuffixToMs)
        .build()
        .rewriteFiles();
  }
}
