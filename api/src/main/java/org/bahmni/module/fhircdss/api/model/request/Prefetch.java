package org.bahmni.module.fhircdss.api.model.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bahmni.module.fhircdss.api.model.serializers.FhirResourceSerializer;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class Prefetch implements Serializable {
    @JsonSerialize(using = FhirResourceSerializer.class)
    private Patient patient;

    @JsonSerialize(using = FhirResourceSerializer.class)
    private Bundle conditions;

    @JsonSerialize(using = FhirResourceSerializer.class)
    private Bundle draftMedicationRequests;

}
