package org.bahmni.module.fhircdss.api.model.request;

import ca.uhn.fhir.parser.IParser;
import org.bahmni.module.fhircdss.api.util.CdssUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

public class Prefetch {
    private Patient patient;

    private Bundle conditions;

    private Bundle draftMedicationRequests;

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Bundle getConditions() {
        return conditions;
    }

    public void setConditions(Bundle conditions) {
        this.conditions = conditions;
    }

    public Bundle getDraftMedicationRequests() {
        return draftMedicationRequests;
    }

    public void setDraftMedicationRequests(Bundle draftMedicationRequests) {
        this.draftMedicationRequests = draftMedicationRequests;
    }

    @Override
    public String toString() {
        IParser parser = CdssUtils.getFhirJsonParser();

        return "{" +
                "\"patient\" : " + parser.encodeResourceToString(patient) +
                ", \"conditions\" : " + parser.encodeResourceToString(conditions) +
                ", \"draftMedicationRequests\" : " + parser.encodeResourceToString(draftMedicationRequests) +
                '}';
    }

}
