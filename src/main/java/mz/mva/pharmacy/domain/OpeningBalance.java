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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Opening Balance (v9 §32) — initial/go-live stock, distinct from a normal supplier receipt, elevated authorization. */
@Entity
@Table(name = "opening_balances")
public class OpeningBalance {

    @Id
    private UUID id;

    @Column(name = "balance_number", nullable = false, unique = true, updatable = false)
    private String balanceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private PharmacyStore store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(columnDefinition = "TEXT")
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

    protected OpeningBalance() {
    }

    public OpeningBalance(
            UUID id,
            String balanceNumber,
            PharmacyStore store,
            Medication medication,
            String batchNumber,
            int quantity,
            UUID createdByStaffId) {
        this.id = id;
        this.balanceNumber = balanceNumber;
        this.store = store;
        this.medication = medication;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
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

    public String getBalanceNumber() {
        return balanceNumber;
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

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
