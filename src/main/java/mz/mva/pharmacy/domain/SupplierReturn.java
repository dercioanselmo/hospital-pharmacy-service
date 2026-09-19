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
import java.time.LocalDate;
import java.util.UUID;

/** Supplier Return (v9 §32) — linked to the original GRN whenever possible; never rewrites it. */
@Entity
@Table(name = "supplier_returns")
public class SupplierReturn {

    @Id
    private UUID id;

    @Column(name = "return_number", nullable = false, unique = true, updatable = false)
    private String returnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_grn_id")
    private GoodsReceivedNote originalGrn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private PharmacyStore store;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "return_type")
    private String returnType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @Column(name = "created_by_staff_id", nullable = false)
    private UUID createdByStaffId;

    @Column(name = "posted_by_staff_id")
    private UUID postedByStaffId;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SupplierReturn() {
    }

    public SupplierReturn(
            UUID id,
            String returnNumber,
            GoodsReceivedNote originalGrn,
            PharmacyStore store,
            LocalDate returnDate,
            String returnType,
            String reason,
            UUID createdByStaffId) {
        this.id = id;
        this.returnNumber = returnNumber;
        this.originalGrn = originalGrn;
        this.store = store;
        this.returnDate = returnDate;
        this.returnType = returnType;
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

    public void post(UUID staffId) {
        this.status = DocumentStatus.POSTED;
        this.postedByStaffId = staffId;
        this.postedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getReturnNumber() {
        return returnNumber;
    }

    public GoodsReceivedNote getOriginalGrn() {
        return originalGrn;
    }

    public PharmacyStore getStore() {
        return store;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getReason() {
        return reason;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public UUID getCreatedByStaffId() {
        return createdByStaffId;
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
