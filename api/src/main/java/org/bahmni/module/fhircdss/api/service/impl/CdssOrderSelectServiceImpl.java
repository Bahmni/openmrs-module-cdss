package org.bahmni.module.fhircdss.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.fhircdss.api.client.RestClient;
import org.bahmni.module.fhircdss.api.exception.CdssException;
import org.bahmni.module.fhircdss.api.model.alert.CDSCard;
import org.bahmni.module.fhircdss.api.model.request.CDSRequest;
import org.bahmni.module.fhircdss.api.model.request.Prefetch;
import org.bahmni.module.fhircdss.api.service.CdssOrderSelectService;
import org.bahmni.module.fhircdss.api.validator.BundleRequestValidator;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CdssOrderSelectServiceImpl implements CdssOrderSelectService {

    @Autowired
    private BundleRequestValidator bundleRequestValidator;

    @Autowired
    private PatientRequestBuilder patientRequestBuilder;

    @Autowired
    private ConditionsRequestBuilder conditionsRequestBuilder;

    @Autowired
    private MedicationRequestBuilder medicationRequestBuilder;

    @Autowired
    private RestClient restClient;

    @Override
    public List<CDSCard> validateInteractions(String serviceName, Bundle bundle) {
        bundleRequestValidator.validate(bundle);

        Prefetch prefetch = Prefetch.builder().patient(patientRequestBuilder.build(bundle))
                                              .conditions(conditionsRequestBuilder.build(bundle))
                                              .draftMedicationRequests(medicationRequestBuilder.build(bundle))
                                              .build();
        CDSRequest cdsRequest = CDSRequest.builder()
                .hook(serviceName)
                .prefetch(prefetch)
                .build();
        return checkForContraindications(serviceName, cdsRequest);
    }

    private List<CDSCard> checkForContraindications(String serviceName, CDSRequest cdsRequest) {
        String cdssEndPoint = getCdssGlobalProperty(CDSS_SERVER_BASE_URL_GLOBAL_PROP) + serviceName;
        String responseStr = restClient.getResponse(cdssEndPoint, cdsRequest);
        return toCdsAlerts(Optional.of(responseStr).orElseThrow(CdssException::new));
    }

    private List<CDSCard> toCdsAlerts(String responseStr) {
        Map<String, List<CDSCard>> cards = null;
        try {
            cards = new ObjectMapper().readValue(responseStr, java.util.Map.class);
        } catch (JsonProcessingException e) {
            throw new CdssException(e);
        }
        return cards.get("cards");
    }

    private String getCdssGlobalProperty(String propertyName) {
        String propertyValue = Context.getAdministrationService().getGlobalProperty(propertyName);
        if (StringUtils.isBlank(propertyValue))
            throw new CdssException();
        return propertyValue;
    }
}