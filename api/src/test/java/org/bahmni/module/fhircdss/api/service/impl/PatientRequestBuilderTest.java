package org.bahmni.module.fhircdss.api.service.impl;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPatientService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PatientRequestBuilderTest {

    @InjectMocks
    private PatientRequestBuilder patientRequestBuilder;

    @Mock
    private FhirPatientService fhirPatientService;

    private static final String PATIENT_FAMILY_NAME = "John";

    private static final String PATIENT_GIVEN_NAME = "Doe";

    private static final String PATIENT_UUID = "3434gh32-34h3j4-34jk34-3422h";

    @Test
    public void shouldReturnFhirPatient_whenMedicationRequestHasPatientReference() throws Exception {
        Bundle mockBundle = getMockRequestBundle();
        when(fhirPatientService.get(anyString())).thenReturn(getMockPatient());
        Patient result = patientRequestBuilder.build(mockBundle);

        assertThat(result, notNullValue());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getId(), equalTo(PATIENT_UUID));
        verify(fhirPatientService, times(1)).get("dc9444c6-ad55-4200-b6e9-407e025eb948");
    }

    private Bundle getMockRequestBundle() throws Exception {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource("request_bundle.json").toURI());
        String mockStr = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        return FhirContext.forR4().newJsonParser().parseResource(Bundle.class, mockStr);
    }

    private Patient getMockPatient() {
        HumanName humanName = new HumanName();
        humanName.addGiven(PATIENT_GIVEN_NAME);
        humanName.setFamily(PATIENT_FAMILY_NAME);

        Patient fhirPatient = new org.hl7.fhir.r4.model.Patient();
        fhirPatient.setId(PATIENT_UUID);
        fhirPatient.addName(humanName);

        return fhirPatient;

    }
}