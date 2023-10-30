package org.bahmni.module.fhircdss.api.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;

import java.util.List;
import java.util.stream.Collectors;

public class CdssUtils {

    private CdssUtils() {
    }

    private static IParser parser = null;

    public static String getPatientUuidFromMedicationRequestOrConditionEntryOrPatientEntry(Bundle bundle) {
        List<Bundle.BundleEntryComponent> medicationEntries = bundle.getEntry().stream().filter(entry -> ResourceType.MedicationRequest.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        Reference subject = null;
        if (!medicationEntries.isEmpty()) {
            MedicationRequest medicationRequest = (MedicationRequest) medicationEntries.get(0).getResource();
            subject = medicationRequest.getSubject();
            return subject.getReferenceElement().getIdPart();
        }
        List<Bundle.BundleEntryComponent> conditionEntries = bundle.getEntry().stream().filter(entry -> ResourceType.Condition.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        if (!conditionEntries.isEmpty()) {
            Condition conditionEntry = (Condition) conditionEntries.get(0).getResource();
            subject = conditionEntry.getSubject();
            return subject.getReferenceElement().getIdPart();
        }

        List<Bundle.BundleEntryComponent> patientEntries = bundle.getEntry().stream().filter(entry -> ResourceType.Patient.equals(entry.getResource().getResourceType())).collect(Collectors.toList());
        Patient patientEntry = (Patient) patientEntries.get(0).getResource();
        return patientEntry.getIdElement().getIdPart();
    }

    public static IParser getFhirJsonParser() {
        if (parser == null) {
            synchronized (CdssUtils.class) {
                if (parser == null) {
                    parser = FhirContext.forR4().newJsonParser();
                    parser.setPrettyPrint(true);
                }
            }
        }
        return parser;
    }
}
