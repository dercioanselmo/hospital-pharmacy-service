package mz.mva.pharmacy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** One line of a {@link GoodsReceivedNote} — a bulk GRN may contain multiple medication/batch lines (v9 §24). */
@Entity
@Table(name = "grn_lines")
public class GrnLine {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", nullable = false)
    private GoodsReceivedNote grn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "free_quantity", nullable = false)
    private int freeQuantity;

    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "sale_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal salePrice;

    protected GrnLine() {
    }

    public GrnLine(
            UUID id,
            GoodsReceivedNote grn,
            Medication medication,
            String batchNumber,
            LocalDate expiryDate,
            int quantity,
            int freeQuantity,
            BigDecimal costPrice,
            BigDecimal salePrice) {
        this.id = id;
        this.grn = grn;
        this.medication = medication;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.freeQuantity = freeQuantity;
        this.costPrice = costPrice;
        this.salePrice = salePrice;
    }

    /** Purchased + free = received stock quantity (v9 §24) — the total actually added to the batch. */
    public int totalReceivedQuantity() {
        return quantity + freeQuantity;
    }

    public UUID getId() {
        return id;
    }

    public GoodsReceivedNote getGrn() {
        return grn;
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

    public int getQuantity() {
        return quantity;
    }

    public int getFreeQuantity() {
        return freeQuantity;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }
}
