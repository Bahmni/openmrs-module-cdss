package org.bahmni.module.fhircdss.api.validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.fhircdss.api.exception.CdssException;
import org.bahmni.module.fhircdss.api.model.cdsservice.CDSServices;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static org.bahmni.module.fhircdss.api.service.CdssOrderSelectService.CDSS_SERVER_BASE_URL_GLOBAL_PROP;

@Component
public class CdsServiceValidator {

    private RestTemplate restTemplate;

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    public CdsServiceValidator(@Qualifier("cdssRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void validate(String serviceName) {
        String cdssServiceEndPoint = getCdssServiceEndPoint();
        CDSServices cdsServices = restTemplate.getForObject(cdssServiceEndPoint, CDSServices.class);
        boolean serviceExists = cdsServices.getServices().stream().filter(service -> service.getId().equals(serviceName)).count() > 0;
        if (!serviceExists) {
            String errorMessage = String.format("CDSService %s unavailable in the configured CDSS System", serviceName);
            log.error(errorMessage);
            throw new CdssException(errorMessage);
        }
    }

    private String getCdssServiceEndPoint() {
        String propertyValue = Context.getAdministrationService().getGlobalProperty(CDSS_SERVER_BASE_URL_GLOBAL_PROP);
        if (StringUtils.isBlank(propertyValue)) {
            log.error("CDSS Host URL is empty");
            throw new CdssException("CDSS Host URL is empty");
        }
        return propertyValue;
    }
}
