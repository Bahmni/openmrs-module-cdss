package org.bahmni.module.fhircdss.api.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bahmni.module.fhircdss.api.exception.CdssException;
import org.bahmni.module.fhircdss.api.model.request.CDSRequest;
import org.openmrs.api.APIException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

@Component
public class RestClient {

    public String getResponse(String cdssEndPoint, CDSRequest cdsRequest) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(URI.create(cdssEndPoint));
            httpPost.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(cdsRequest)));

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
            return httpclient.execute(httpPost, responseHandler);
        } catch (IOException e) {
            throw new CdssException(e);
        }
    }
}
