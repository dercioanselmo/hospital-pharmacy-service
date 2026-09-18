package mz.mva.pharmacy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * An effective-dated price for one {@link Medication}. {@code effectiveTo == null} means
 * "current, open-ended" — the previous open version is closed whenever a new one is added.
 */
@Entity
@Table(name = "medication_price_versions")
public class MedicationPriceVersion {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "price_list_code", nullable = false)
    private String priceListCode = "STANDARD";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MedicationPriceVersion() {
    }

    public MedicationPriceVersion(UUID id, Medication medication, BigDecimal price, LocalDate effectiveFrom, String priceListCode) {
        this.id = id;
        this.medication = medication;
        this.price = price;
        this.effectiveFrom = effectiveFrom;
        this.priceListCode = priceListCode;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Medication getMedication() {
        return medication;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getPriceListCode() {
        return priceListCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
