package mz.mva.pharmacy.client;

/** Raised when the call to hospital-clinical-service fails (network error or non-2xx response). */
public class ClinicalServiceException extends RuntimeException {

    public ClinicalServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClinicalServiceException(String message) {
        super(message);
    }
}
