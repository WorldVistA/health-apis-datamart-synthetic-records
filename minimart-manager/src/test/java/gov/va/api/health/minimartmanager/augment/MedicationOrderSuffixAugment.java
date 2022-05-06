package gov.va.api.health.minimartmanager.augment;

import static com.google.common.base.Preconditions.checkNotNull;

import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.lighthouse.datamart.CompositeCdwId;

public class MedicationOrderSuffixAugment {
  public static void main(String[] args) {
    Augmentation.forResources(DatamartMedicationOrder.class)
        .whenMatching(mo -> mo.cdwId().endsWith(":FP"))
        .transform(MedicationOrderSuffixAugment::updateFrankenPatientSuffixToO)
        .build()
        .rewriteFiles();
  }

  static DatamartMedicationOrder updateFrankenPatientSuffixToO(
      Augmentation.Context<DatamartMedicationOrder> ctx) {
    DatamartMedicationOrder dm = ctx.resource();
    String cdwId = dm.cdwId();

    dm.cdwId(cdwId.substring(0, cdwId.length() - 3) + ":O");
    checkNotNull(CompositeCdwId.fromCdwId(dm.cdwId()));
    return dm;
  }
}
