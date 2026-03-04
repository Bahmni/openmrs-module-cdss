/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. SNOMED International. SNOMED International is a registered trademark
 * and the SNOMED International graphic logo is a trademark of SNOMED International.
 */

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
        log.info("Stopped Bahmni FHIR CDSS Integration module");
    }

}
