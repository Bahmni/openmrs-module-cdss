package org.bahmni.module.fhircdss.api.service.impl;

import org.apache.log4j.Logger;
import org.bahmni.module.fhircdss.api.util.OrderFrequencyValue;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.OrderFrequency;
import org.openmrs.api.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class OrderFrequencyResolver {

    private static final Logger logger = Logger.getLogger(OrderFrequencyResolver.class);

    private OrderService orderService;

    @Autowired
    public OrderFrequencyResolver(OrderService orderService) {
        this.orderService = orderService;
    }

    public OrderFrequencyValue resolve(String frequencyText) {
        if (frequencyText == null || frequencyText.trim().isEmpty()) {
            return null;
        }
        List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies(false);
        if (orderFrequencies == null) {
            return null;
        }
        for (OrderFrequency orderFrequency : orderFrequencies) {
            if (orderFrequency == null || orderFrequency.getFrequencyPerDay() == null) {
                continue;
            }
            Concept concept = orderFrequency.getConcept();
            if (concept == null) {
                continue;
            }
            for (ConceptName conceptName : concept.getNames()) {
                if (conceptName != null && normalize(frequencyText).equals(normalize(conceptName.getName()))) {
                    return OrderFrequencyValue.fromFrequencyPerDay(orderFrequency.getFrequencyPerDay());
                }
            }
        }
        logger.warn("No order frequency found in OpenMRS for frequency text: '" + frequencyText + "'");
        return null;
    }

    private String normalize(String text) {
        if (text == null) {
            return null;
        }
        return text.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ENGLISH);
    }
}