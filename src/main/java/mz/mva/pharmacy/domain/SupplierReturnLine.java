package mz.mva.pharmacy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/** One line of a {@link SupplierReturn} — may cover only selected items/partial quantities from a GRN (v9 §32). */
@Entity
@Table(name = "supplier_return_lines")
public class SupplierReturnLine {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_return_id", nullable = false)
    private SupplierReturn supplierReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @Column(nullable = false)
    private int quantity;

    protected SupplierReturnLine() {
    }

    public SupplierReturnLine(UUID id, SupplierReturn supplierReturn, Medication medication, String batchNumber, int quantity) {
        this.id = id;
        this.supplierReturn = supplierReturn;
        this.medication = medication;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
    }

    public UUID getId() {
        return id;
    }

    public SupplierReturn getSupplierReturn() {
        return supplierReturn;
    }

    public Medication getMedication() {
        return medication;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public int getQuantity() {
        return quantity;
    }
}
