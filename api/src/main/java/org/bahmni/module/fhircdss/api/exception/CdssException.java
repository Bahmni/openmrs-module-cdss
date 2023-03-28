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
