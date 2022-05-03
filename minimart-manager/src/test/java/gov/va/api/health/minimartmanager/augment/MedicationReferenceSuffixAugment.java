package gov.va.api.health.minimartmanager.augment;

import static com.google.common.base.Preconditions.checkArgument;

import gov.va.api.health.dataquery.service.controller.medication.DatamartMedication;
import gov.va.api.health.dataquery.service.controller.medicationorder.DatamartMedicationOrder;
import gov.va.api.health.dataquery.service.controller.medicationstatement.DatamartMedicationStatement;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.util.Objects;
import java.util.Optional;

public class MedicationReferenceSuffixAugment {
  static void addIdSuffix(DatamartReference ref) {
    if (ref == null) {
      return;
    }
    checkArgument("Medication".equals(ref.type().orElse(null)));
    checkArgument(ref.reference().isPresent());
    String id = ref.reference().get();
    if (!id.endsWith(":L")) {
      ref.reference(Optional.of(id + ":L"));
    }
  }

  static DatamartMedication addMedicationIdSuffixToMedication(
      Augmentation.Context<DatamartMedication> ctx) {
    DatamartMedication dm = ctx.resource();
    if (!dm.cdwId().endsWith(":L")) {
      dm.cdwId(dm.cdwId() + ":L");
    }
    return dm;
  }

  static DatamartMedicationOrder addMedicationIdSuffixToMedicationOrder(
      Augmentation.Context<DatamartMedicationOrder> ctx) {
    DatamartMedicationOrder dm = ctx.resource();
    addIdSuffix(dm.medication());
    return dm;
  }

  static DatamartMedicationStatement addMedicationIdSuffixToMedicationStatement(
      Augmentation.Context<DatamartMedicationStatement> ctx) {
    DatamartMedicationStatement dm = ctx.resource();
    addIdSuffix(dm.medication());
    return dm;
  }

  public static void main(String[] args) {
    Augmentation.forResources(DatamartMedicationOrder.class)
        .whenMatching(Objects::nonNull)
        .transform(MedicationReferenceSuffixAugment::addMedicationIdSuffixToMedicationOrder)
        .build()
        .rewriteFiles();
    Augmentation.forResources(DatamartMedicationStatement.class)
        .whenMatching(Objects::nonNull)
        .transform(MedicationReferenceSuffixAugment::addMedicationIdSuffixToMedicationStatement)
        .build()
        .rewriteFiles();
    Augmentation.forResources(DatamartMedication.class)
        .whenMatching(Objects::nonNull)
        .transform(MedicationReferenceSuffixAugment::addMedicationIdSuffixToMedication)
        .build()
        .rewriteFiles();
  }
}
