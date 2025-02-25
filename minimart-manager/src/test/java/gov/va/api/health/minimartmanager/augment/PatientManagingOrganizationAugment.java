package gov.va.api.health.minimartmanager.augment;

import gov.va.api.health.dataquery.service.controller.organization.DatamartOrganization;
import gov.va.api.health.dataquery.service.controller.patient.DatamartPatient;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class PatientManagingOrganizationAugment {
  private static final List<Optional<String>> ORGANIZATION_REFERENCES = loadOrganizations();

  static DatamartPatient addManagingOrganization(Augmentation.Context<DatamartPatient> ctx) {
    return ctx.resource().managingOrganization(ctx.random(ORGANIZATION_REFERENCES));
  }

  private static List<Optional<String>> loadOrganizations() {
    List<Optional<String>> o =
        ReferenceLoader.loadReferencesFor(
                DatamartOrganization.class,
                dm ->
                    DatamartReference.builder().reference(Optional.ofNullable(dm.cdwId())).build())
            .stream()
            .map(d -> d.flatMap(DatamartReference::reference))
            .collect(Collectors.toList());
    o.add(Optional.empty());
    return o;
  }

  public static void main(String[] args) {
    Augmentation.forResources(DatamartPatient.class)
        .whenMatching(Objects::nonNull)
        .transform(PatientManagingOrganizationAugment::addManagingOrganization)
        .build()
        .rewriteFiles();
  }
}
