package gov.va.api.health.minimartmanager.transformers;

import static gov.va.api.health.dataquery.service.controller.Transformers.allBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.emptyToNull;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.dataquery.service.controller.EnumSearcher;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization.Status;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization.VaccinationProtocols;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization.VaccineCode;
import gov.va.api.health.dstu2.api.datatypes.Annotation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Immunization;
import gov.va.api.health.dstu2.api.resources.Immunization.Reaction;
import gov.va.api.health.dstu2.api.resources.Immunization.VaccinationProtocol;
import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.minimartmanager.FhirToDatamartUtils;
import gov.va.api.lighthouse.datamart.DatamartCoding;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class F2DImmunizationTransformer {
  FhirToDatamartUtils fauxIds;

  private static DatamartCoding toCoding(Coding coding) {
    if (coding == null || allBlank(coding.system(), coding.code(), coding.display())) {
      return null;
    }
    return DatamartCoding.builder()
        .system(Optional.ofNullable(coding.system()))
        .code(Optional.ofNullable(coding.code()))
        .display(Optional.ofNullable(coding.display()))
        .build();
  }

  private Instant date(String date) {
    if (date == null) {
      return null;
    }
    return Instant.parse(date);
  }

  public DatamartImmunization fhirToDatamart(Immunization immunization) {
    return DatamartImmunization.builder()
        .cdwId(fauxIds.unmask("Immunization", immunization.id()))
        .status(status(immunization.status(), immunization._status()))
        .date(date(immunization.date()))
        .vaccineCode(vaccineCode(immunization.vaccineCode()))
        .patient(fauxIds.toDatamartReferenceWithCdwId(immunization.patient()).get())
        .wasNotGiven(immunization.wasNotGiven())
        .performer(fauxIds.toDatamartReferenceWithCdwId(immunization.performer()))
        .requester(fauxIds.toDatamartReferenceWithCdwId(immunization.requester()))
        .encounter(fauxIds.toDatamartReferenceWithCdwId(immunization.encounter()))
        .location(fauxIds.toDatamartReferenceWithCdwId(immunization.location()))
        .note(note(immunization.note()))
        .reaction(fauxIds.toDatamartReferenceWithCdwId(reaction(immunization.reaction())))
        .vaccinationProtocols(vaccinationProtocols(immunization.vaccinationProtocol()))
        .build();
  }

  private Optional<String> note(List<Annotation> note) {
    if (note == null || note.isEmpty()) {
      return null;
    }
    return Optional.of(note.get(0).text());
  }

  private Reference reaction(List<Reaction> reactions) {
    if (reactions == null) {
      return null;
    }
    if (reactions.isEmpty()) {
      return null;
    }
    return reactions.get(0).detail();
  }

  private Status status(Immunization.Status status, Extension extension) {
    if (status == null && extension == null) {
      return null;
    }
    if (status != null) {
      return EnumSearcher.of(DatamartImmunization.Status.class).find(status.toString());
    }
    return Status.data_absent_reason_unsupported;
  }

  private Optional<VaccinationProtocols> vaccinationProtocols(
      List<VaccinationProtocol> vaccinationProtocol) {
    if (vaccinationProtocol == null || vaccinationProtocol.isEmpty()) {
      return null;
    }
    return Optional.of(
        VaccinationProtocols.builder()
            .series(Optional.ofNullable(vaccinationProtocol.get(0).series()))
            .seriesDoses(Optional.ofNullable(vaccinationProtocol.get(0).seriesDoses()))
            .build());
  }

  private VaccineCode vaccineCode(CodeableConcept vaccineCode) {
    if (vaccineCode == null) {
      return null;
    }
    var codings =
        emptyToNull(
            Safe.stream(vaccineCode.coding())
                .map(F2DImmunizationTransformer::toCoding)
                .collect(toList()));
    if (codings == null) {
      return null;
    }
    return VaccineCode.builder().coding(codings).build();
  }
}
