package org.bahmni.module.fhircdss.web.controller;

import org.bahmni.module.fhircdss.api.model.alert.CDSAlert;
import org.bahmni.module.fhircdss.api.service.CdssOrderSelectService;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/cdss")
public class CdssValidationController extends BaseRestController {

    private CdssOrderSelectService orderSelectService;

    @Autowired
    public CdssValidationController(CdssOrderSelectService orderSelectService) {
        this.orderSelectService = orderSelectService;
    }

    @PostMapping
    @ResponseBody
    public List<CDSAlert> validate(@RequestParam(value = "service") String serviceName, @RequestBody Bundle bundle) {
        return orderSelectService.validateInteractions(serviceName, bundle);
    }

}
