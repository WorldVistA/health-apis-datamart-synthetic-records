package gov.va.api.health.minimartmanager.augment;

import gov.va.api.health.dataquery.service.controller.condition.DatamartCondition;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.DatamartDiagnosticReport;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation;
import gov.va.api.health.dataquery.service.controller.procedure.DatamartProcedure;
import java.util.Objects;
import java.util.Optional;

public class EncounterReferenceRemovalAugments {
  public static void main(String[] args) {
    Augmentation.forResources(DatamartCondition.class)
        .whenMatching(Objects::nonNull)
        .transform(EncounterReferenceRemovalAugments::removeEncounterFromCondition)
        .build()
        .rewriteFiles();
    Augmentation.forResources(DatamartDiagnosticReport.class)
        .whenMatching(Objects::nonNull)
        .transform(EncounterReferenceRemovalAugments::removeEncounterFromDiagnosticReport)
        .build()
        .rewriteFiles();
    Augmentation.forResources(DatamartImmunization.class)
        .whenMatching(Objects::nonNull)
        .transform(EncounterReferenceRemovalAugments::removeEncounterFromImmunization)
        .build()
        .rewriteFiles();
    Augmentation.forResources(DatamartObservation.class)
        .whenMatching(Objects::nonNull)
        .transform(EncounterReferenceRemovalAugments::removeEncounterFromObservation)
        .build()
        .rewriteFiles();
    Augmentation.forResources(DatamartProcedure.class)
        .whenMatching(Objects::nonNull)
        .transform(EncounterReferenceRemovalAugments::removeEncounterFromProcedure)
        .build()
        .rewriteFiles();
  }

  static DatamartCondition removeEncounterFromCondition(
      Augmentation.Context<DatamartCondition> ctx) {
    ctx.resource().encounter(Optional.empty());
    return ctx.resource();
  }

  static DatamartDiagnosticReport removeEncounterFromDiagnosticReport(
      Augmentation.Context<DatamartDiagnosticReport> ctx) {
    ctx.resource().encounter(Optional.empty());
    return ctx.resource();
  }

  static DatamartImmunization removeEncounterFromImmunization(
      Augmentation.Context<DatamartImmunization> ctx) {
    ctx.resource().encounter(Optional.empty());
    return ctx.resource();
  }

  static DatamartObservation removeEncounterFromObservation(
      Augmentation.Context<DatamartObservation> ctx) {
    ctx.resource().encounter(Optional.empty());
    return ctx.resource();
  }

  static DatamartProcedure removeEncounterFromProcedure(
      Augmentation.Context<DatamartProcedure> ctx) {
    ctx.resource().encounter(Optional.empty());
    return ctx.resource();
  }
}
