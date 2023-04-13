package org.bahmni.module.fhircdss.api.validator;

import org.bahmni.module.fhircdss.api.exception.CdssException;
import org.bahmni.module.fhircdss.api.model.cdsservice.Service;
import org.bahmni.module.fhircdss.api.model.cdsservice.Services;
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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static org.bahmni.module.fhircdss.api.service.CdssOrderSelectService.CDSS_SERVER_BASE_URL_GLOBAL_PROP;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class})
@PowerMockIgnore("javax.management.*")
public class CdsServiceValidatorTest {

    @InjectMocks
    private CdsServiceValidator cdsServiceValidator;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    @Qualifier("adminService")
    AdministrationService administrationService;

    @Mock
    private UserContext userContext;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
    }

    @Test
    public void shouldPassValidateService_whenCdssEngineAccessedWithValidService() {
        Services services = getServices();
        when(restTemplate.getForObject("http://localhost", Services.class)).thenReturn(services);
        when(administrationService.getGlobalProperty(CDSS_SERVER_BASE_URL_GLOBAL_PROP)).thenReturn("http://localhost");

        cdsServiceValidator.validate("medication-order-select");
    }

    @Test
    public void shouldThrowException_whenCdssEngineAccessedWithInvalidService() {
        Services services = getServices();
        when(restTemplate.getForObject("http://localhost", Services.class)).thenReturn(services);
        when(administrationService.getGlobalProperty(CDSS_SERVER_BASE_URL_GLOBAL_PROP)).thenReturn("http://localhost");

        thrown.expect(CdssException.class);
        thrown.expectMessage("Service invalid-service unavailable in the configured CDSS System");

        cdsServiceValidator.validate("invalid-service");
    }

    @Test
    public void shouldThrowException_whenGlobalPropertyForCdssEndPointIsNotConfigured() {
        Services services = getServices();
        when(restTemplate.getForObject("http://localhost", Services.class)).thenReturn(services);
        when(administrationService.getGlobalProperty(CDSS_SERVER_BASE_URL_GLOBAL_PROP)).thenReturn("");

        thrown.expect(CdssException.class);
        thrown.expectMessage("CDSS Host URL is empty");

        cdsServiceValidator.validate("medication-order-select");
    }

    private Services getServices() {
        Services services = new Services();
        Service medicationOrderSelect = new Service();
        medicationOrderSelect.setId("medication-order-select");
        Service drugDrugService = new Service();
        drugDrugService.setId("drug-drug-service");
        services.setServices(Arrays.asList(medicationOrderSelect, drugDrugService));
        return services;
    }
}