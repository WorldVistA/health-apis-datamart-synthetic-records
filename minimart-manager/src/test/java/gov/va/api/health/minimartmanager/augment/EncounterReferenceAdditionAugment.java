package gov.va.api.health.minimartmanager.augment;

import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReport;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EncounterReferenceAdditionAugment {
  private static final List<Optional<DatamartReference>> ENCOUNTERS =
      List.of(
          encounterBuilder("800022196947:O"),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          encounterBuilder("2147483643:I"),
          Optional.empty(),
          Optional.empty(),
          Optional.empty());

  static DatamartCondition addEncounterToCondition(Augmentation.Context<DatamartCondition> ctx) {
    ctx.resource().encounter(ctx.random(ENCOUNTERS));
    return ctx.resource();
  }

  static DatamartDiagnosticReport addEncounterToDiagnosticReport(
      Augmentation.Context<DatamartDiagnosticReport> ctx) {
    ctx.resource().encounter(ctx.random(ENCOUNTERS));
    return ctx.resource();
  }

  static DatamartImmunization addEncounterToImmunization(
      Augmentation.Context<DatamartImmunization> ctx) {
    ctx.resource().encounter(ctx.random(ENCOUNTERS));
    return ctx.resource();
  }

  static DatamartObservation addEncounterToObservation(
      Augmentation.Context<DatamartObservation> ctx) {
    ctx.resource().encounter(ctx.random(ENCOUNTERS));
    return ctx.resource();
  }

  static DatamartProcedure addEncounterToProcedure(Augmentation.Context<DatamartProcedure> ctx) {
    ctx.resource().encounter(ctx.random(ENCOUNTERS));
    return ctx.resource();
  }

  static Optional<DatamartReference> encounterBuilder(String id) {
    return Optional.of(DatamartReference.of().type("Encounter").reference(id).build());
  }

  public static void main(String[] args) {
    Augmentation.forResources(DatamartCondition.class)
        .whenMatching(Objects::nonNull)
        .whenMatching(p -> "43000199".equals(p.patient().reference().get()))
        .transform(EncounterReferenceAdditionAugment::addEncounterToCondition)
        .build()
        .rewriteFiles();
    Augmentation.forResources(DatamartDiagnosticReport.class)
        .whenMatching(Objects::nonNull)
        .whenMatching(p -> "43000199".equals(p.patient().reference().get()))
        .transform(EncounterReferenceAdditionAugment::addEncounterToDiagnosticReport)
        .build()
        .rewriteFiles();
    Augmentation.forResources(DatamartImmunization.class)
        .whenMatching(Objects::nonNull)
        .whenMatching(p -> "43000199".equals(p.patient().reference().get()))
        .transform(EncounterReferenceAdditionAugment::addEncounterToImmunization)
        .build()
        .rewriteFiles();
    Augmentation.forResources(DatamartObservation.class)
        .whenMatching(Objects::nonNull)
        .whenMatching(p -> "43000199".equals(p.subject().get().reference().get()))
        .transform(EncounterReferenceAdditionAugment::addEncounterToObservation)
        .build()
        .rewriteFiles();
    Augmentation.forResources(DatamartProcedure.class)
        .whenMatching(Objects::nonNull)
        .whenMatching(p -> "43000199".equals(p.patient().reference().get()))
        .transform(EncounterReferenceAdditionAugment::addEncounterToProcedure)
        .build()
        .rewriteFiles();
  }
}
