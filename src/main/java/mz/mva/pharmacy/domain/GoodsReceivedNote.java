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

/**
 * Bulk GRN (v9 §24/§32) — receipt of purchased pharmacy stock. Saving never changes stock;
 * posting is the controlled, idempotent operation that does (v9 §32's explicit rule).
 */
@Entity
@Table(name = "goods_received_notes")
public class GoodsReceivedNote {

    @Id
    private UUID id;

    @Column(name = "grn_number", nullable = false, unique = true, updatable = false)
    private String grnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private PharmacyStore store;

    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

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

    protected GoodsReceivedNote() {
    }

    public GoodsReceivedNote(
            UUID id, String grnNumber, PharmacyStore store, String supplierName, LocalDate receiptDate, UUID createdByStaffId) {
        this.id = id;
        this.grnNumber = grnNumber;
        this.store = store;
        this.supplierName = supplierName;
        this.receiptDate = receiptDate;
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

    public String getGrnNumber() {
        return grnNumber;
    }

    public PharmacyStore getStore() {
        return store;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public LocalDate getReceiptDate() {
        return receiptDate;
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
