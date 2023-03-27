package org.bahmni.module.fhircdss.api.model.request;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

public class Prefetch {
    Patient patient;

    Bundle conditions;

    Bundle draftMedicationRequests;

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
        IParser parser = FhirContext.forR4().newJsonParser();
        parser.setPrettyPrint(true);

        return "{" +
                "\"patient\" : " + parser.encodeResourceToString(patient) +
                ", \"conditions\" : " + parser.encodeResourceToString(conditions) +
                ", \"draftMedicationRequests\" : " + parser.encodeResourceToString(draftMedicationRequests) +
                '}';
    }

}
