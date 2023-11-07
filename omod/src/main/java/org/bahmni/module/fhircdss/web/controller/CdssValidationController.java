package org.bahmni.module.fhircdss.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bahmni.module.fhircdss.api.exception.CdssException;
import org.bahmni.module.fhircdss.api.exception.DrugDosageException;
import org.bahmni.module.fhircdss.api.model.alert.CDSAlert;
import org.bahmni.module.fhircdss.api.service.CdssOrderSelectService;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> validate(@RequestParam(value = "service") String serviceName, @RequestBody Bundle bundle) {
        try {
            return new ResponseEntity<>(orderSelectService.validateInteractions(serviceName, bundle), HttpStatus.ACCEPTED);
        } catch (DrugDosageException e) {
            SimpleObject response = new SimpleObject();
            response.add("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (JsonProcessingException e) {
            throw new CdssException(e.getMessage());
        }
    }

}
