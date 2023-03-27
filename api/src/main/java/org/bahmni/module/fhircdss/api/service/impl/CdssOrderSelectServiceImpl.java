package org.bahmni.module.fhircdss.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bahmni.module.fhircdss.api.model.alert.CDSCard;
import org.bahmni.module.fhircdss.api.model.request.CDSRequest;
import org.bahmni.module.fhircdss.api.model.request.Prefetch;
import org.bahmni.module.fhircdss.api.service.CdssOrderSelectService;
import org.bahmni.module.fhircdss.api.service.PayloadGenerator;
import org.bahmni.module.fhircdss.api.validator.BundleRequestValidator;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.api.APIException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CdssOrderSelectServiceImpl implements CdssOrderSelectService {

    @Autowired
    BundleRequestValidator bundleRequestValidator;

    @Autowired
    private List<PayloadGenerator> payloadGenerators;

    @Override
    public List<CDSCard> checkContraindications(String service, Bundle bundle) {
        bundleRequestValidator.validate(bundle);

        CDSRequest cdsRequest = new CDSRequest();
        Prefetch prefetch = new Prefetch();
        cdsRequest.setPrefetch(prefetch);

        for (PayloadGenerator payloadGenerator : payloadGenerators) {
            payloadGenerator.generate(bundle, cdsRequest);
        }

        cdsRequest.setHook(service);

        System.out.println(cdsRequest);
        return validateInteractions(cdsRequest);
    }

    private List<CDSCard> validateInteractions(CDSRequest cdsRequest) {
        String cdssEndPoint = "https://cdss-dev.snomed.mybahmni.in/cds-services/medication-order-select";
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(URI.create(cdssEndPoint));
            httpPost.setEntity(new StringEntity(cdsRequest.toString()));

            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Accept", "application/json");
            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new APIException("Unexpected response status: " + status);
                }
            };
            String responseStr = httpclient.execute(httpPost, responseHandler);
            return toCdsAlerts(Optional.of(responseStr).orElseThrow(() -> new RuntimeException()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<CDSCard> toCdsAlerts(String responseStr) throws JsonProcessingException {
        Map<String, List<CDSCard>> cards = new ObjectMapper().readValue(responseStr, java.util.Map.class);
        return cards.get("cards");
    }
}