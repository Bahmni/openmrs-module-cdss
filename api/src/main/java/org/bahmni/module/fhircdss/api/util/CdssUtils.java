package org.bahmni.module.fhircdss.api.util;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;

import java.util.List;
import java.util.stream.Collectors;

public class CdssUtils {

    public static String getPatientUuid(Bundle bundle) {
        List<Bundle.BundleEntryComponent> medicationEntries = bundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        MedicationRequest medicationRequest = (MedicationRequest) medicationEntries.get(0).getResource();
        Reference subject = medicationRequest.getSubject();
        int index = subject.getReference().indexOf("/") + 1;
        return subject.getReference().substring(index);
    }
}
