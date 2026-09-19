package mz.mva.pharmacy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * The append-only ledger — "the system must always be able to answer why stock changed, which
 * document caused it, who posted it, ... the before/after quantity" (v9 §32). Never mutated after
 * creation.
 */
@Entity
@Table(name = "pharmacy_inventory_transactions")
public class PharmacyInventoryTransaction {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private PharmacyTransactionType transactionType;

    @Column(name = "medication_id", nullable = false)
    private UUID medicationId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "quantity_before", nullable = false)
    private int quantityBefore;

    @Column(name = "quantity_after", nullable = false)
    private int quantityAfter;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "staff_id", nullable = false)
    private UUID staffId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PharmacyInventoryTransaction() {
    }

    public PharmacyInventoryTransaction(
            UUID id,
            PharmacyTransactionType transactionType,
            UUID medicationId,
            UUID storeId,
            String batchNumber,
            int quantity,
            int quantityBefore,
            int quantityAfter,
            String referenceType,
            UUID referenceId,
            String reason,
            UUID staffId) {
        this.id = id;
        this.transactionType = transactionType;
        this.medicationId = medicationId;
        this.storeId = storeId;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.reason = reason;
        this.staffId = staffId;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public PharmacyTransactionType getTransactionType() {
        return transactionType;
    }

    public UUID getMedicationId() {
        return medicationId;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getQuantityBefore() {
        return quantityBefore;
    }

    public int getQuantityAfter() {
        return quantityAfter;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public String getReason() {
        return reason;
    }

    public UUID getStaffId() {
        return staffId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
