package org.bahmni.module.fhircdss.api.service.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.fhir2.api.FhirAllergyIntoleranceService;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class AllergiesRequestBuilderTest {

    @InjectMocks
    private AllergiesRequestBuilder allergiesRequestBuilder;

    @Mock
    private FhirAllergyIntoleranceService fhirAllergyIntoleranceService;

    @Test
    public void shouldIncludeActiveAllergies_whenPatientHasActiveAndInactiveAllergies() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle();

        AllergyIntolerance activeAllergy = new AllergyIntolerance();
        CodeableConcept activeClinicalStatus = new CodeableConcept();
        activeClinicalStatus.setCoding(Collections.singletonList(new Coding("http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical", "active", "Active")));
        activeAllergy.setClinicalStatus(activeClinicalStatus);
        Coding allergenCoding = new Coding("http://snomed.info/sct", "890458001", "Penicillin-containing product");
        activeAllergy.getCode().setCoding(Collections.singletonList(allergenCoding));

        AllergyIntolerance inactiveAllergy = new AllergyIntolerance();
        CodeableConcept inactiveClinicalStatus = new CodeableConcept();
        inactiveClinicalStatus.setCoding(Collections.singletonList(new Coding("http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical", "inactive", "Inactive")));
        inactiveAllergy.setClinicalStatus(inactiveClinicalStatus);

        when(fhirAllergyIntoleranceService.searchForAllergies(any())).thenReturn(new SimpleBundleProvider(Arrays.asList(activeAllergy, inactiveAllergy)));

        Bundle allergiesBundle = allergiesRequestBuilder.build(mockRequestBundle);

        List<Bundle.BundleEntryComponent> allergyEntries = allergiesBundle.getEntry();
        assertEquals(1, allergyEntries.size());
        assertEquals("890458001", ((AllergyIntolerance) allergyEntries.get(0).getResource()).getCode().getCoding().get(0).getCode());
    }

    @Test
    public void shouldIncludeZeroAllergies_whenPatientHasNoAllergies() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle();

        when(fhirAllergyIntoleranceService.searchForAllergies(any())).thenReturn(new SimpleBundleProvider());

        Bundle allergiesBundle = allergiesRequestBuilder.build(mockRequestBundle);

        assertEquals(0, allergiesBundle.getEntry().size());
    }

    private Bundle getMockRequestBundle() throws Exception {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource("request_bundle.json").toURI());
        String mockStr = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        return FhirContext.forR4().newJsonParser().parseResource(Bundle.class, mockStr);
    }
}