package org.bahmni.module.fhircdss.api.model.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
@Builder
public class CDSRequest implements Serializable {

    private String hook;

    private Prefetch prefetch;

}
