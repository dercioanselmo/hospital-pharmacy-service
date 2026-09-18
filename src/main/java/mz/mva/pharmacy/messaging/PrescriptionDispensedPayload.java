package mz.mva.pharmacy.messaging;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * {@code catalogItemCode}/{@code catalogItemName}/{@code priceAtCompletion} were added for
 * automatic charge capture (Service Catalog Pricing sub-phase E) — {@code priceAtCompletion} is
 * the medication's unit price, {@code null} when not yet configured, meaning "not billable."
 */
public record PrescriptionDispensedPayload(
        UUID dispensingRecordId,
        UUID prescriptionOrderId,
        UUID encounterId,
        UUID patientId,
        int quantityDispensed,
        String catalogItemCode,
        String catalogItemName,
        BigDecimal priceAtCompletion) {
}
