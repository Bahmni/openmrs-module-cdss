package org.bahmni.module.fhircdss.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.fhircdss.api.exception.CdssException;
import org.bahmni.module.fhircdss.api.model.alert.CDSCard;
import org.bahmni.module.fhircdss.api.model.request.CDSRequest;
import org.bahmni.module.fhircdss.api.model.request.Prefetch;
import org.bahmni.module.fhircdss.api.service.CdssOrderSelectService;
import org.bahmni.module.fhircdss.api.validator.BundleRequestValidator;
import org.bahmni.module.fhircdss.api.validator.CdsServiceValidator;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CdssOrderSelectServiceImpl implements CdssOrderSelectService {
    private static Logger logger = Logger.getLogger(CdssOrderSelectServiceImpl.class);
    @Autowired
    private BundleRequestValidator bundleRequestValidator;

    @Autowired
    private CdsServiceValidator cdsServiceValidator;

    @Autowired
    private PatientRequestBuilder patientRequestBuilder;

    @Autowired
    private ConditionsRequestBuilder conditionsRequestBuilder;

    @Autowired
    private MedicationRequestBuilder medicationRequestBuilder;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public List<CDSCard> validateInteractions(String serviceName, Bundle bundle) {
        cdsServiceValidator.validate(serviceName);
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
        String cdssEndPoint = getCdssGlobalProperty(CDSS_SERVER_BASE_URL_GLOBAL_PROP) + "/" + serviceName;
        ResponseEntity<Map> responseEntityMap = restTemplate.postForEntity(cdssEndPoint, getEntityRequest(cdsRequest), java.util.Map.class);
        int status = responseEntityMap.getStatusCode().value();
        if (status >= 200 && status < 300) {
            Map<String, List<CDSCard>> cards = responseEntityMap.getBody();
            return Optional.of(cards.get("cards")).orElseThrow(CdssException::new);
        } else {
            logger.error("Call to CDS server failed with response status code = " + status);
            throw new CdssException();
        }
    }

    private HttpEntity<String> getEntityRequest(CDSRequest cdsRequest) {
        HttpEntity<String> cdsEntityRequest = null;
        try {
            cdsEntityRequest = new HttpEntity<String>(new ObjectMapper().writeValueAsString(cdsRequest), getHeaders());
        } catch (JsonProcessingException e) {
            logger.error("Error in transforming CDS request into JSON");
            throw new CdssException();
        }
        return cdsEntityRequest;
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private String getCdssGlobalProperty(String propertyName) {
        String propertyValue = Context.getAdministrationService().getGlobalProperty(propertyName);
        if (StringUtils.isBlank(propertyValue)) {
            logger.error("Global property '" + propertyName + "' value is missing");
            throw new CdssException();
        }
        return propertyValue;
    }
}