package gov.va.api.health.minimartmanager.augment;

import gov.va.api.health.dataquery.service.controller.practitionerrole.DatamartPractitionerRole;
import java.util.Objects;

public class PractitionerRoleRoleAugments {
  public static void main(String[] args) {
    Augmentation.forResources(DatamartPractitionerRole.class)
        .whenMatching(Objects::nonNull)
        .transform(PractitionerRoleRoleAugments::role)
        .build()
        .rewriteFiles();
  }

  static DatamartPractitionerRole role(Augmentation.Context<DatamartPractitionerRole> ctx) {
    if (ctx.resource().role().isEmpty()) {
      System.out.println("No roles exist for practitionerRole.cdwId: " + ctx.resource().cdwId());
      return ctx.resource();
    }

    for (DatamartPractitionerRole.Role role : ctx.resource().role()) {
      if (role.coding().isPresent() && ctx.random().nextBoolean()) {
        var coding = role.coding().get();
        role.text(coding.display());
      }
    }

    return ctx.resource();
  }
}
