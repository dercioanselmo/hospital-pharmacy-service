package mz.mva.pharmacy.service;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.Medication;
import mz.mva.pharmacy.dto.MedicationDto;
import mz.mva.pharmacy.repository.MedicationRepository;
import org.springframework.stereotype.Service;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public List<MedicationDto> findAll() {
        return medicationRepository.findAll().stream().map(MedicationDto::from).toList();
    }

    public MedicationDto create(MedicationDto dto) {
        Medication medication = new Medication(
                UUID.randomUUID(), dto.code(), dto.name(), dto.form(), dto.strength(), dto.onHandQuantity());
        return MedicationDto.from(medicationRepository.save(medication));
    }

    Medication getOrThrow(UUID id) {
        return medicationRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Medication not found: " + id));
    }

    /**
     * Decrements the simple on-hand quantity. Not real batch/lot inventory
     * (deferred to a future Phase 5b) — just enough to make dispensing a
     * real business rule rather than a no-op.
     */
    void decrementStock(UUID medicationId, int quantity) {
        Medication medication = getOrThrow(medicationId);
        if (medication.getOnHandQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Medication " + medication.getCode() + " has only " + medication.getOnHandQuantity()
                            + " on hand, cannot dispense " + quantity);
        }
        medication.setOnHandQuantity(medication.getOnHandQuantity() - quantity);
        medicationRepository.save(medication);
    }
}
