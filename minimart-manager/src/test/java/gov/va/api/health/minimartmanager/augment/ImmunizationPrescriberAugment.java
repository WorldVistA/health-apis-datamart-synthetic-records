package gov.va.api.health.minimartmanager.augment;

import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.minimartmanager.augment.Augmentation.Context;
import java.util.Optional;

public class ImmunizationPrescriberAugment {
  public static void main(String[] args) {
    Augmentation.forResources(DatamartImmunization.class)
        .whenMatching(p -> p.performer().isPresent())
        .transform(ImmunizationPrescriberAugment::removePrescriber)
        .build()
        .rewriteFiles();
  }

  static DatamartImmunization removePrescriber(Context<DatamartImmunization> ctx) {
    ctx.resource().performer(Optional.empty());
    return ctx.resource();
  }
}
