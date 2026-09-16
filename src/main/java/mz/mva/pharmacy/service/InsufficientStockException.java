package mz.mva.pharmacy.service;

/** Raised when a dispensing request exceeds a medication's on-hand quantity. */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
