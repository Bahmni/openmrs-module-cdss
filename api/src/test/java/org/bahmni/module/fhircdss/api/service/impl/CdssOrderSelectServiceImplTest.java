package org.bahmni.module.fhircdss.api.service.impl;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.module.fhircdss.api.exception.CdssException;
import org.bahmni.module.fhircdss.api.exception.DrugDosageException;
import org.bahmni.module.fhircdss.api.model.alert.CDSAlert;
import org.bahmni.module.fhircdss.api.validator.BundleRequestValidator;
import org.bahmni.module.fhircdss.api.validator.CdsServiceValidator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.util.LocaleUtility;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.bahmni.module.fhircdss.api.service.CdssOrderSelectService.CDSS_SERVER_BASE_URL_GLOBAL_PROP;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class, LocaleUtility.class})
@PowerMockIgnore("javax.management.*")
public class CdssOrderSelectServiceImplTest {

    @Mock
    @Qualifier("adminService")
    AdministrationService administrationService;

    @InjectMocks
    private CdssOrderSelectServiceImpl cdssOrderSelectService;

    @Mock
    private BundleRequestValidator bundleRequestValidator;

    @Mock
    private CdsServiceValidator cdsServiceValidator;

    @Mock
    private PatientRequestBuilder patientRequestBuilder;

    @Mock
    private ConditionsRequestBuilder conditionsRequestBuilder;

    @Mock
    private MedicationRequestBuilder medicationRequestBuilder;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserContext userContext;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(CDSS_SERVER_BASE_URL_GLOBAL_PROP)).thenReturn("http://localhost");
    }

    @Test
    public void shouldGetAlerts_whenStatinPrescribedForPatientHavingLiverDisease() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle();
        doNothing().when(bundleRequestValidator).validate(mockRequestBundle);
        doNothing().when(cdsServiceValidator).validate(anyString());
        when(patientRequestBuilder.build(mockRequestBundle)).thenReturn(new Patient());
        when(conditionsRequestBuilder.build(mockRequestBundle)).thenReturn(new Bundle());
        when(medicationRequestBuilder.build(mockRequestBundle)).thenReturn(new Bundle());
        when(restTemplate.postForEntity(anyString(), any(), refEq(java.util.Map.class))).thenReturn(getResponse());

        List<CDSAlert> cdsAlerts = cdssOrderSelectService.validateInteractions("medication-order-select", mockRequestBundle);

        verify(patientRequestBuilder, times(1)).build(mockRequestBundle);
        verify(conditionsRequestBuilder, times(1)).build(mockRequestBundle);
        verify(medicationRequestBuilder, times(1)).build(mockRequestBundle);
        assertEquals(1, cdsAlerts.size());
    }
    @Test
    public void shouldThrowException_whenCdssServiceThrowErrorResponseStatusExceptionWith4xx() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle();
        doNothing().when(bundleRequestValidator).validate(mockRequestBundle);
        doNothing().when(cdsServiceValidator).validate(anyString());
        when(patientRequestBuilder.build(mockRequestBundle)).thenReturn(new Patient());
        when(conditionsRequestBuilder.build(mockRequestBundle)).thenReturn(new Bundle());
        when(medicationRequestBuilder.build(mockRequestBundle)).thenReturn(new Bundle());
        when(restTemplate.postForEntity(anyString(), any(), refEq(java.util.Map.class))).thenThrow(new HttpClientErrorException(HttpStatus.PRECONDITION_FAILED, " dummy status", getMockHttpClientErrorExceptionWith4xx().getBytes(), null));
        thrown.expect(DrugDosageException.class);
        thrown.expectMessage("dummy dosage exception");
        cdssOrderSelectService.validateInteractions("medication-order-select", mockRequestBundle);

    }
    @Test
    public void shouldThrowException_whenCdssServiceThrowErrorResponseStatusExceptionOtherThanWith4xx() throws Exception {
        Bundle mockRequestBundle = getMockRequestBundle();
        doNothing().when(bundleRequestValidator).validate(mockRequestBundle);
        doNothing().when(cdsServiceValidator).validate(anyString());
        when(patientRequestBuilder.build(mockRequestBundle)).thenReturn(new Patient());
        when(conditionsRequestBuilder.build(mockRequestBundle)).thenReturn(new Bundle());
        when(medicationRequestBuilder.build(mockRequestBundle)).thenReturn(new Bundle());
        when(restTemplate.postForEntity(anyString(), any(), refEq(java.util.Map.class))).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "dummy status", getMockHttpClientErrorExceptionWith5xx().getBytes(), null));
        thrown.expect(CdssException.class);
        thrown.expectMessage("dummy dosage exception");
        cdssOrderSelectService.validateInteractions("medication-order-select", mockRequestBundle);

    }

    private Bundle getMockRequestBundle() throws Exception {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource("request_bundle.json").toURI());
        String mockStr = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        return FhirContext.forR4().newJsonParser().parseResource(Bundle.class, mockStr);
    }

    private ResponseEntity<Map> getResponse() throws Exception {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource("response_warning.json").toURI());
        String responseStr = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        Map<String, List<CDSAlert>> alerts = null;
        try {
            alerts = new ObjectMapper().readValue(responseStr, java.util.Map.class);
        } catch (JsonProcessingException e) {
            throw new CdssException(e);
        }
        ResponseEntity<Map> responseEntityMap = new ResponseEntity<>(alerts, HttpStatus.OK);
        return responseEntityMap;
    }
    private String getMockHttpClientErrorExceptionWith4xx() {
        return "{\n" +
                "  \"timestamp\" : \"2023-11-07T11:31:11.847+00:00\",\n" +
                "  \"status\" : 412,\n" +
                "  \"error\" : \"Precondition Failed\",\n" +
                "  \"message\" : \"dummy dosage exception\",\n" +
                "  \"path\" : \"/cds-services/medication-order-select\"\n" +
                "}";
    }
    private String getMockHttpClientErrorExceptionWith5xx() {
        return "{\n" +
                "  \"timestamp\" : \"2023-11-07T11:31:11.847+00:00\",\n" +
                "  \"status\" : 500,\n" +
                "  \"error\" : \"Precondition Failed\",\n" +
                "  \"message\" : \"dummy dosage exception\",\n" +
                "  \"path\" : \"/cds-services/medication-order-select\"\n" +
                "}";
    }
}