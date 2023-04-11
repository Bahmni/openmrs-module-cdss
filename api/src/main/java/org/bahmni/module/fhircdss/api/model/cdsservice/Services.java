package org.bahmni.module.fhircdss.api.model.cdsservice;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Services {
    private List<Service> services;
}
