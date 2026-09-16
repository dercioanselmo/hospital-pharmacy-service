package mz.mva.pharmacy.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.DispensingRecord;
import mz.mva.pharmacy.domain.PrescriptionOrder;
import mz.mva.pharmacy.dto.DispensingRecordDto;
import mz.mva.pharmacy.repository.DispensingRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transactional at the class level — reads the lazy {@code prescriptionOrder} association. */
@Service
@Transactional
public class DispensingRecordService {

    private final DispensingRecordRepository dispensingRecordRepository;
    private final PrescriptionOrderService prescriptionOrderService;
    private final MedicationService medicationService;

    public DispensingRecordService(
            DispensingRecordRepository dispensingRecordRepository,
            PrescriptionOrderService prescriptionOrderService,
            MedicationService medicationService) {
        this.dispensingRecordRepository = dispensingRecordRepository;
        this.prescriptionOrderService = prescriptionOrderService;
        this.medicationService = medicationService;
    }

    public List<DispensingRecordDto> findByPrescriptionOrder(UUID prescriptionOrderId) {
        return dispensingRecordRepository.findByPrescriptionOrderId(prescriptionOrderId).stream()
                .map(DispensingRecordDto::from)
                .toList();
    }

    /**
     * Dispensing the medication decrements Pharmacy's own on-hand quantity and
     * moves the PrescriptionOrder DISPENSING -> DISPENSED (local only — this
     * mirrors Laboratory's Result / Radiology's Report creation step, which
     * also doesn't sync ClinicalOrder). Completion (below) is separate.
     */
    public DispensingRecordDto create(UUID prescriptionOrderId, int quantityDispensed, UUID staffId) {
        PrescriptionOrder order = prescriptionOrderService.getOrThrow(prescriptionOrderId);
        medicationService.decrementStock(order.getMedication().getId(), quantityDispensed);
        DispensingRecord record = new DispensingRecord(UUID.randomUUID(), order, quantityDispensed, staffId);
        DispensingRecord saved = dispensingRecordRepository.save(record);
        prescriptionOrderService.markDispensed(prescriptionOrderId);
        return DispensingRecordDto.from(saved);
    }

    /** Completion confirms the patient handoff/counseling and syncs ClinicalOrder to COMPLETED. */
    public DispensingRecordDto complete(UUID dispensingRecordId, UUID staffId, String bearerToken) {
        DispensingRecord record = dispensingRecordRepository
                .findById(dispensingRecordId)
                .orElseThrow(() -> new NotFoundException("Dispensing record not found: " + dispensingRecordId));
        if (record.getCompletedAt() != null) {
            throw new InvalidStateTransitionException(
                    "Dispensing record " + dispensingRecordId + " is already completed");
        }
        record.setCompletedByStaffId(staffId);
        record.setCompletedAt(Instant.now());
        DispensingRecord saved = dispensingRecordRepository.save(record);
        prescriptionOrderService.markCompleted(record.getPrescriptionOrder().getId(), bearerToken);
        return DispensingRecordDto.from(saved);
    }
}
