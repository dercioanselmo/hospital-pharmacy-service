package mz.mva.pharmacy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * The dispensing act itself — mirrors hospital-laboratory-service's Result /
 * hospital-radiology-service's Report: created mid-pipeline, then verified
 * (here: "completed", i.e. patient counseled and the handoff finished) by a
 * separate action that syncs ClinicalOrder to COMPLETED.
 */
@Entity
@Table(name = "dispensing_records")
public class DispensingRecord {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_order_id", nullable = false)
    private PrescriptionOrder prescriptionOrder;

    @Column(name = "quantity_dispensed", nullable = false)
    private int quantityDispensed;

    /** Cross-service reference to hospital-staff-service's staff_members.id — the dispensing pharmacist. */
    @Column(name = "dispensed_by_staff_id", nullable = false)
    private UUID dispensedByStaffId;

    @Column(name = "dispensed_at", nullable = false)
    private Instant dispensedAt;

    /** Cross-service reference to hospital-staff-service's staff_members.id — who completed the handoff. */
    @Column(name = "completed_by_staff_id")
    private UUID completedByStaffId;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected DispensingRecord() {
    }

    public DispensingRecord(
            UUID id, PrescriptionOrder prescriptionOrder, int quantityDispensed, UUID dispensedByStaffId) {
        this.id = id;
        this.prescriptionOrder = prescriptionOrder;
        this.quantityDispensed = quantityDispensed;
        this.dispensedByStaffId = dispensedByStaffId;
    }

    @PrePersist
    void onCreate() {
        this.dispensedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public PrescriptionOrder getPrescriptionOrder() {
        return prescriptionOrder;
    }

    public int getQuantityDispensed() {
        return quantityDispensed;
    }

    public UUID getDispensedByStaffId() {
        return dispensedByStaffId;
    }

    public Instant getDispensedAt() {
        return dispensedAt;
    }

    public UUID getCompletedByStaffId() {
        return completedByStaffId;
    }

    public void setCompletedByStaffId(UUID completedByStaffId) {
        this.completedByStaffId = completedByStaffId;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
