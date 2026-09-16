package mz.mva.pharmacy.domain;

/**
 * RECEIVED (claimed) -> VERIFIED (pharmacist confirmed dosage/safety) ->
 * DISPENSING (being prepared) -> DISPENSED (handed over, stock decremented,
 * local only) -> COMPLETED (patient counseled, Clinical order completed).
 * Mirrors the 5-state shape of Laboratory's LabOrderStatus and Radiology's
 * ImagingOrderStatus with domain-appropriate names.
 */
public enum PrescriptionOrderStatus {
    RECEIVED,
    VERIFIED,
    DISPENSING,
    DISPENSED,
    COMPLETED
}
