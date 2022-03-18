package gov.va.api.health.minimartmanager.minimart.augments;

import gov.va.api.health.dataquery.service.controller.devicerequest.DatamartDeviceRequest;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.lighthouse.datamart.DatamartReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class DeviceRequestCPAPAugments {

  private static final List<String> DEV_REQ_IDS_TO_CREATE =
      List.of("-6703:C", "-4827:C", "-5937:C", "-8392:C");

  public static void main(String[] args) {
    Augmentation.forResources(DatamartDeviceRequest.class)
        .whenMatching(Objects::nonNull)
        .whenMatching(devReq -> DEV_REQ_IDS_TO_CREATE.contains(devReq.cdwId()))
        .transform(DeviceRequestCPAPAugments::rewriteDeviceRequest)
        .build()
        .rewriteFiles();
  }

  static DatamartDeviceRequest rewriteDeviceRequest(
      Augmentation.Context<DatamartDeviceRequest> ctx) {
    var resource = ctx.resource();
    var requesters =
        ReferenceLoader.loadReferencesFor(
            DatamartPractitioner.class,
            dm -> {
              String whoDis =
                  dm.name().prefix().orElse("")
                      + " "
                      + dm.name().given()
                      + " "
                      + dm.name().family()
                      + " "
                      + dm.name().suffix().orElse("");
              return DatamartReference.builder()
                  .type(Optional.of("Practitioner"))
                  .reference(Optional.ofNullable(dm.cdwId()))
                  .display(Optional.of(whoDis))
                  .build();
            });

    Random rand = new Random();

    var requester = requesters.get(rand.nextInt(requesters.size()));

    return DatamartDeviceRequest.builder()
        .cdwId(resource.cdwId())
        .patient(resource.patient())
        .occurrenceDateTime(resource.occurrenceDateTime())
        .status(Optional.of("COMPLETE"))
        .requester(requester)
        .codeCodeableConcept(
            DatamartDeviceRequest.CodeableConcept.builder()
                .text("PROSTHETICS REQUEST - CPAP/BIPAP")
                .build())
        .build();
  }
}
