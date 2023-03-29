package org.bahmni.module.fhircdss.api.model.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CDSRequest {

    private String hook;

    private Prefetch prefetch;

    @Override
    public String toString() {
        return "{" +
                "\"hook\" : \"" + hook + "\"" +
                ",\"prefetch\" : " + prefetch.toString() +
                '}';
    }
}
