package mz.mva.pharmacy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Physical-vs-system stock reconciliation (v9 §32). Requires approval before posting — "ordinary
 * pharmacy users must not be able to freely increase/decrease stock without the appropriate
 * permission."
 */
@Entity
@Table(name = "stock_adjustments")
public class StockAdjustment {

    @Id
    private UUID id;

    @Column(name = "adjustment_number", nullable = false, unique = true, updatable = false)
    private String adjustmentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private PharmacyStore store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdjustmentDirection direction;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "quantity_before")
    private Integer quantityBefore;

    @Column(name = "quantity_after")
    private Integer quantityAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdjustmentStatus status = AdjustmentStatus.DRAFT;

    @Column(name = "created_by_staff_id", nullable = false)
    private UUID createdByStaffId;

    @Column(name = "approved_by_staff_id")
    private UUID approvedByStaffId;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "posted_by_staff_id")
    private UUID postedByStaffId;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StockAdjustment() {
    }

    public StockAdjustment(
            UUID id,
            String adjustmentNumber,
            PharmacyStore store,
            Medication medication,
            String batchNumber,
            AdjustmentDirection direction,
            int quantity,
            String reason,
            UUID createdByStaffId) {
        this.id = id;
        this.adjustmentNumber = adjustmentNumber;
        this.store = store;
        this.medication = medication;
        this.batchNumber = batchNumber;
        this.direction = direction;
        this.quantity = quantity;
        this.reason = reason;
        this.createdByStaffId = createdByStaffId;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void approve(UUID staffId) {
        this.status = AdjustmentStatus.APPROVED;
        this.approvedByStaffId = staffId;
        this.approvedAt = Instant.now();
    }

    public void post(UUID staffId, int before, int after) {
        this.status = AdjustmentStatus.POSTED;
        this.postedByStaffId = staffId;
        this.postedAt = Instant.now();
        this.quantityBefore = before;
        this.quantityAfter = after;
    }

    public UUID getId() {
        return id;
    }

    public String getAdjustmentNumber() {
        return adjustmentNumber;
    }

    public PharmacyStore getStore() {
        return store;
    }

    public Medication getMedication() {
        return medication;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public AdjustmentDirection getDirection() {
        return direction;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getReason() {
        return reason;
    }

    public Integer getQuantityBefore() {
        return quantityBefore;
    }

    public Integer getQuantityAfter() {
        return quantityAfter;
    }

    public AdjustmentStatus getStatus() {
        return status;
    }

    public UUID getCreatedByStaffId() {
        return createdByStaffId;
    }

    public UUID getApprovedByStaffId() {
        return approvedByStaffId;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public UUID getPostedByStaffId() {
        return postedByStaffId;
    }

    public Instant getPostedAt() {
        return postedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
