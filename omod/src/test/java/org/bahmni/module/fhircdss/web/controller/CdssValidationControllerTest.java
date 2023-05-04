package org.bahmni.module.fhircdss.web.controller;

import ca.uhn.fhir.context.FhirContext;
import org.bahmni.module.fhircdss.api.exception.CdssException;
import org.bahmni.module.fhircdss.api.model.alert.CDSAlert;
import org.bahmni.module.fhircdss.api.model.alert.CDSIndicator;
import org.bahmni.module.fhircdss.api.model.alert.CDSSource;
import org.bahmni.module.fhircdss.api.service.CdssOrderSelectService;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CdssValidationControllerTest {

    @InjectMocks
    private CdssValidationController cdssValidationController;

    @Mock
    private CdssOrderSelectService cdssOrderSelectService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldGetAlerts_whenStatinPrescribedForPatientHavingLiverDisease() throws Exception {
        Bundle mockRequestBundle = getMockBundle("request_bundle.json");
        when(cdssOrderSelectService.validateInteractions("medication-order-select", mockRequestBundle)).thenReturn(getMockAlert());

        List<CDSAlert> alerts = cdssValidationController.validate("medication-order-select", mockRequestBundle);

        assertNotNull(alerts);
        assertEquals(1, alerts.size());
        assertEquals(CDSIndicator.warning, alerts.get(0).getIndicator());
        assertEquals("Wikipedia", alerts.get(0).getSource().getLabel());
    }

    @Test
    public void shouldThrowException_whenGlobalPropertyForCdssEndPointIsNotConfigured() throws Exception {
        Bundle mockRequestBundle = getMockBundle("request_bundle.json");
        when(cdssOrderSelectService.validateInteractions("medication-order-select", mockRequestBundle)).thenThrow(new CdssException("CDSS Host URL in empty"));

        thrown.expect(CdssException.class);
        thrown.expectMessage("CDSS Host URL in empty");

        cdssValidationController.validate("medication-order-select", mockRequestBundle);
    }

    private Bundle getMockBundle(String fileName) throws Exception {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource(fileName).toURI());
        String mockString = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        return FhirContext.forR4().newJsonParser().parseResource(Bundle.class, mockString);
    }

    private List<CDSAlert> getMockAlert() {
        CDSAlert criticalAlert = new CDSAlert();
        CDSSource source = new CDSSource();
        source.setLabel("Wikipedia");
        source.setUrl("https://en.wikipedia.org/wiki/Atorvastatin#Contraindications");
        criticalAlert.setSource(source);
        criticalAlert.setIndicator(CDSIndicator.warning);
        criticalAlert.setSummary("Contraindication: Atorvastatin-containing product with patient condition Steatosis of liver.");
        return Collections.singletonList(criticalAlert);
    }
}