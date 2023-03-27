package org.bahmni.module.fhircdss.web.controller;

import org.bahmni.module.fhircdss.api.model.alert.CDSCard;
import org.bahmni.module.fhircdss.api.service.CdssOrderSelectService;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/cdss")
public class CdssValidationController extends BaseRestController {

    private CdssOrderSelectService orderSelectService;

    @Autowired
    public CdssValidationController(CdssOrderSelectService orderSelectService) {
        this.orderSelectService = orderSelectService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public List<CDSCard> validate(@RequestParam(value = "interaction") String interaction, @RequestBody Bundle bundle) {
        return orderSelectService.checkContraindications(interaction, bundle);
    }

}
