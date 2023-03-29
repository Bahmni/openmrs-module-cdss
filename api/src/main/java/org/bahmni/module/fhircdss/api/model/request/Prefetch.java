package org.bahmni.module.fhircdss.api.model.request;

import ca.uhn.fhir.parser.IParser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bahmni.module.fhircdss.api.util.CdssUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

@Getter
@Setter
@Builder
public class Prefetch {
    private Patient patient;

    private Bundle conditions;

    private Bundle draftMedicationRequests;

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
