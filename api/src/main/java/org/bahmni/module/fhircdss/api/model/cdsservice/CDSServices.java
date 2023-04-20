package org.bahmni.module.fhircdss.api.model.cdsservice;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CDSServices {
    private List<CDSService> services;
}
