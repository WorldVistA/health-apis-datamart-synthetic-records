package gov.va.api.health.minimartmanager.augment;

import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.minimartmanager.augment.Augmentation.Context;
import java.util.Objects;

public class ImmunizationPrimarySourceAugment {
  static DatamartImmunization addPrimarySource(Context<DatamartImmunization> ctx) {
    ctx.resource().primarySource(true);
    return ctx.resource();
  }

  public static void main(String[] args) {
    Augmentation.forResources(DatamartImmunization.class)
        .whenMatching(Objects::nonNull)
        .transform(ImmunizationPrimarySourceAugment::addPrimarySource)
        .build()
        .rewriteFiles();
  }
}
