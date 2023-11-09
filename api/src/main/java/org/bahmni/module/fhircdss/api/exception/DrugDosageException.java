package org.bahmni.module.fhircdss.api.exception;

public class DrugDosageException extends RuntimeException{
    public DrugDosageException() {
        super();
    }

    public DrugDosageException(String message) {
        super(message);
    }

    public DrugDosageException(String message, Throwable cause) {
        super(message, cause);
    }

    public DrugDosageException(Throwable cause) {
        super(cause);
    }
}
