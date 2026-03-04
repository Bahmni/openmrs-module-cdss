/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. SNOMED International. SNOMED International is a registered trademark
 * and the SNOMED International graphic logo is a trademark of SNOMED International.
 */

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
