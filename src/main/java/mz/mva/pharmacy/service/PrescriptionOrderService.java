package mz.mva.pharmacy.service;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.client.ClinicalOrderClient;
import mz.mva.pharmacy.client.ClinicalOrderView;
import mz.mva.pharmacy.domain.Medication;
import mz.mva.pharmacy.domain.PrescriptionOrder;
import mz.mva.pharmacy.domain.PrescriptionOrderStatus;
import mz.mva.pharmacy.dto.PrescriptionOrderDto;
import mz.mva.pharmacy.dto.ReceiveOrderRequest;
import mz.mva.pharmacy.repository.PrescriptionOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional at the class level so mapping to {@link PrescriptionOrderDto}
 * (which reads the lazy {@code medication} association) always happens
 * inside an active Hibernate session — same reasoning as
 * hospital-laboratory-service's LabOrderService and
 * hospital-radiology-service's ImagingOrderService.
 */
@Service
@Transactional
public class PrescriptionOrderService {

    private final PrescriptionOrderRepository prescriptionOrderRepository;
    private final MedicationService medicationService;
    private final ClinicalOrderClient clinicalOrderClient;

    public PrescriptionOrderService(
            PrescriptionOrderRepository prescriptionOrderRepository,
            MedicationService medicationService,
            ClinicalOrderClient clinicalOrderClient) {
        this.prescriptionOrderRepository = prescriptionOrderRepository;
        this.medicationService = medicationService;
        this.clinicalOrderClient = clinicalOrderClient;
    }

    @Transactional(readOnly = true)
    public List<PrescriptionOrderDto> findQueue(PrescriptionOrderStatus status) {
        PrescriptionOrderStatus effective = status != null ? status : PrescriptionOrderStatus.RECEIVED;
        return prescriptionOrderRepository.findByStatusOrderByCreatedAtAsc(effective).stream()
                .map(PrescriptionOrderDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionOrderDto> findByPatient(UUID patientId) {
        return prescriptionOrderRepository.findByPatientIdOrderByCreatedAtAsc(patientId).stream()
                .map(PrescriptionOrderDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PrescriptionOrderDto findById(UUID id) {
        return PrescriptionOrderDto.from(getOrThrow(id));
    }

    /** Receives (claims) a PLACED ClinicalOrder into Pharmacy's own queue — the Work Queue "claim" action. */
    public PrescriptionOrderDto receive(ReceiveOrderRequest request, String bearerToken) {
        if (prescriptionOrderRepository.findByClinicalOrderId(request.clinicalOrderId()).isPresent()) {
            throw new InvalidStateTransitionException(
                    "Clinical order " + request.clinicalOrderId() + " has already been received by Pharmacy");
        }

        ClinicalOrderView clinicalOrder = clinicalOrderClient.fetch(request.clinicalOrderId(), bearerToken);
        Medication medication = medicationService.getOrThrow(request.medicationId());

        clinicalOrderClient.claim(request.clinicalOrderId(), request.staffId(), bearerToken);

        PrescriptionOrder order = new PrescriptionOrder(
                UUID.randomUUID(),
                clinicalOrder.id(),
                clinicalOrder.encounterId(),
                clinicalOrder.patientId(),
                clinicalOrder.orderingStaffId(),
                medication,
                request.quantityPrescribed(),
                request.dosageInstructions(),
                request.staffId());
        return PrescriptionOrderDto.from(prescriptionOrderRepository.save(order));
    }

    /** Pharmacist confirms dosage/safety before dispensing begins. */
    public PrescriptionOrderDto verify(UUID id, UUID staffId, String bearerToken) {
        PrescriptionOrder order = getOrThrow(id);
        requireStatus(order, PrescriptionOrderStatus.RECEIVED, "verify");
        order.setVerifiedByStaffId(staffId);
        order.setStatus(PrescriptionOrderStatus.VERIFIED);
        clinicalOrderClient.ready(order.getClinicalOrderId(), bearerToken);
        return PrescriptionOrderDto.from(prescriptionOrderRepository.save(order));
    }

    /** Pharmacist begins preparing/counting the medication. */
    public PrescriptionOrderDto startDispensing(UUID id, String bearerToken) {
        PrescriptionOrder order = getOrThrow(id);
        requireStatus(order, PrescriptionOrderStatus.VERIFIED, "start dispensing for");
        order.setStatus(PrescriptionOrderStatus.DISPENSING);
        clinicalOrderClient.start(order.getClinicalOrderId(), bearerToken);
        return PrescriptionOrderDto.from(prescriptionOrderRepository.save(order));
    }

    /** Called by DispensingRecordService once the medication has been handed over — not its own endpoint. */
    PrescriptionOrderDto markDispensed(UUID id) {
        PrescriptionOrder order = getOrThrow(id);
        requireStatus(order, PrescriptionOrderStatus.DISPENSING, "dispense");
        order.setStatus(PrescriptionOrderStatus.DISPENSED);
        return PrescriptionOrderDto.from(prescriptionOrderRepository.save(order));
    }

    /** Called by DispensingRecordService once the patient handoff/counseling is confirmed complete. */
    PrescriptionOrderDto markCompleted(UUID id, String bearerToken) {
        PrescriptionOrder order = getOrThrow(id);
        requireStatus(order, PrescriptionOrderStatus.DISPENSED, "complete");
        order.setStatus(PrescriptionOrderStatus.COMPLETED);
        clinicalOrderClient.complete(order.getClinicalOrderId(), bearerToken);
        return PrescriptionOrderDto.from(prescriptionOrderRepository.save(order));
    }

    PrescriptionOrder getOrThrow(UUID id) {
        return prescriptionOrderRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription order not found: " + id));
    }

    private void requireStatus(PrescriptionOrder order, PrescriptionOrderStatus required, String action) {
        if (order.getStatus() != required) {
            throw new InvalidStateTransitionException(
                    "Prescription order " + order.getId() + " is " + order.getStatus() + ", can only " + action
                            + " a " + required + " order");
        }
    }
}
