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
 * Pharmacy's own fulfillment record for one hospital-clinical-service
 * ClinicalOrder. Clinical owns routing (the ClinicalOrder + its status);
 * Pharmacy owns fulfillment (this record, its DispensingRecord) — see
 * docs/service-boundaries.md. Mirrors hospital-laboratory-service's LabOrder
 * and hospital-radiology-service's ImagingOrder shape.
 */
@Entity
@Table(name = "prescription_orders")
public class PrescriptionOrder {

    @Id
    private UUID id;

    /** Cross-service reference to hospital-clinical-service's clinical_orders.id — by ID only, no FK. */
    @Column(name = "clinical_order_id", nullable = false, unique = true)
    private UUID clinicalOrderId;

    /** Cross-service reference to hospital-clinical-service's encounters.id. */
    @Column(name = "encounter_id", nullable = false)
    private UUID encounterId;

    /** Cross-service reference to hospital-patient-service's patients.id. */
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    /** Cross-service reference to hospital-staff-service's staff_members.id — the prescribing clinician. */
    @Column(name = "ordering_staff_id", nullable = false)
    private UUID orderingStaffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(name = "quantity_prescribed", nullable = false)
    private int quantityPrescribed;

    @Column(name = "dosage_instructions", nullable = false)
    private String dosageInstructions;

    /** Cross-service reference to hospital-staff-service's staff_members.id — who received it. */
    @Column(name = "received_by_staff_id", nullable = false)
    private UUID receivedByStaffId;

    /** Cross-service reference to hospital-staff-service's staff_members.id — the verifying pharmacist. */
    @Column(name = "verified_by_staff_id")
    private UUID verifiedByStaffId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionOrderStatus status = PrescriptionOrderStatus.RECEIVED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PrescriptionOrder() {
    }

    public PrescriptionOrder(
            UUID id,
            UUID clinicalOrderId,
            UUID encounterId,
            UUID patientId,
            UUID orderingStaffId,
            Medication medication,
            int quantityPrescribed,
            String dosageInstructions,
            UUID receivedByStaffId) {
        this.id = id;
        this.clinicalOrderId = clinicalOrderId;
        this.encounterId = encounterId;
        this.patientId = patientId;
        this.orderingStaffId = orderingStaffId;
        this.medication = medication;
        this.quantityPrescribed = quantityPrescribed;
        this.dosageInstructions = dosageInstructions;
        this.receivedByStaffId = receivedByStaffId;
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

    public UUID getId() {
        return id;
    }

    public UUID getClinicalOrderId() {
        return clinicalOrderId;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getOrderingStaffId() {
        return orderingStaffId;
    }

    public Medication getMedication() {
        return medication;
    }

    public int getQuantityPrescribed() {
        return quantityPrescribed;
    }

    public String getDosageInstructions() {
        return dosageInstructions;
    }

    public UUID getReceivedByStaffId() {
        return receivedByStaffId;
    }

    public UUID getVerifiedByStaffId() {
        return verifiedByStaffId;
    }

    public void setVerifiedByStaffId(UUID verifiedByStaffId) {
        this.verifiedByStaffId = verifiedByStaffId;
    }

    public PrescriptionOrderStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionOrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
