package org.bahmni.module.fhircdss;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;

public class CdssActivator extends BaseModuleActivator {

    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    public void started() {
        log.info("Started Bahmni FHIR CDSS Integration module");
    }

    @Override
    public void stopped() {
        log.info("Stopped  Bahmni FHIR CDSS Integration module");
    }

}
