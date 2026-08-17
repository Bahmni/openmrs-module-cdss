/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. SNOMED International. SNOMED International is a registered trademark
 * and the SNOMED International graphic logo is a trademark of SNOMED International.
 */

package org.bahmni.module.fhircdss.api.exception;

public class CdssException extends RuntimeException {

    public CdssException() {
        super();
    }

    public CdssException(String message) {
        super(message);
    }

    public CdssException(String message, Throwable cause) {
        super(message, cause);
    }

    public CdssException(Throwable cause) {
        super(cause);
    }
}
