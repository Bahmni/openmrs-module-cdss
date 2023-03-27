package org.bahmni.module.fhircdss.web.converter;

import ca.uhn.fhir.parser.IParser;
import org.bahmni.module.fhircdss.api.util.CdssUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class FhirBundleMessageConverter extends AbstractHttpMessageConverter<Bundle> {

    private static final String CHARSET = "UTF-8";
    private static final String TYPE = "application";
    private static final String SUBTYPE_1 = "json";


    public FhirBundleMessageConverter() {
        super(new MediaType(TYPE, SUBTYPE_1, Charset.forName(CHARSET)));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Bundle.class.isAssignableFrom(clazz);
    }

    @Override
    protected Bundle readInternal(Class<? extends Bundle> clazz, HttpInputMessage inputMessage) throws HttpMessageNotReadableException, IOException {
        String json = convertStreamToString(inputMessage.getBody());
        IParser parser = CdssUtils.getFhirJsonParser();
        return parser.parseResource(Bundle.class, json);
    }

    @Override
    protected void writeInternal(Bundle resource, HttpOutputMessage outputMessage)
            throws HttpMessageNotWritableException, IOException {
        IParser parser = CdssUtils.getFhirJsonParser();
        String json = parser.encodeResourceToString(resource);
        outputMessage.getBody().write(json.getBytes(StandardCharsets.UTF_8));
    }

    private String convertStreamToString(InputStream inputStream) {
        return new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
