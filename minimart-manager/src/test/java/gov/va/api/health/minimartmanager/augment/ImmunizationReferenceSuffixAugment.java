package gov.va.api.health.minimartmanager.augment;

import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import java.util.Objects;

public class ImmunizationReferenceSuffixAugment {
  static DatamartImmunization addImmunizationIdSuffixToImmunization(
      Augmentation.Context<DatamartImmunization> ctx) {
    DatamartImmunization dm = ctx.resource();
    if (!dm.cdwId().endsWith(":I")) {
      dm.cdwId(dm.cdwId() + ":I");
    }
    return dm;
  }

  public static void main(String[] args) {
    Augmentation.forResources(DatamartImmunization.class)
        .whenMatching(Objects::nonNull)
        .transform(ImmunizationReferenceSuffixAugment::addImmunizationIdSuffixToImmunization)
        .build()
        .rewriteFiles();
  }
}
