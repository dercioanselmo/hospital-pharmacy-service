package mz.mva.pharmacy.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.Medication;
import mz.mva.pharmacy.domain.MedicationPriceVersion;
import mz.mva.pharmacy.dto.CreatePriceVersionRequest;
import mz.mva.pharmacy.dto.MedicationDto;
import mz.mva.pharmacy.dto.PriceVersionDto;
import mz.mva.pharmacy.repository.MedicationPriceVersionRepository;
import mz.mva.pharmacy.repository.MedicationRepository;
import org.springframework.stereotype.Service;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final MedicationPriceVersionRepository priceVersionRepository;

    public MedicationService(
            MedicationRepository medicationRepository, MedicationPriceVersionRepository priceVersionRepository) {
        this.medicationRepository = medicationRepository;
        this.priceVersionRepository = priceVersionRepository;
    }

    public List<MedicationDto> findAll() {
        return medicationRepository.findAll().stream()
                .map(medication -> MedicationDto.from(medication, resolveCurrentPrice(medication.getId(), LocalDate.now())))
                .toList();
    }

    public MedicationDto create(MedicationDto dto) {
        Medication medication = new Medication(
                UUID.randomUUID(), dto.code(), dto.name(), dto.form(), dto.strength(), dto.onHandQuantity());
        return MedicationDto.from(medicationRepository.save(medication));
    }

    public List<PriceVersionDto> findPriceVersions(UUID medicationId) {
        getOrThrow(medicationId);
        return priceVersionRepository.findByMedicationIdOrderByEffectiveFromDesc(medicationId).stream()
                .map(PriceVersionDto::from)
                .toList();
    }

    public PriceVersionDto addPriceVersion(UUID medicationId, CreatePriceVersionRequest request) {
        Medication medication = getOrThrow(medicationId);
        List<MedicationPriceVersion> versions =
                priceVersionRepository.findByMedicationIdOrderByEffectiveFromDesc(medicationId);
        MedicationPriceVersion latest = versions.isEmpty() ? null : versions.get(0);
        if (latest != null && !request.effectiveFrom().isAfter(latest.getEffectiveFrom())) {
            throw new InvalidStateTransitionException(
                    "New price must be effective after the latest existing version's date (" + latest.getEffectiveFrom() + ")");
        }
        if (latest != null && latest.getEffectiveTo() == null) {
            latest.setEffectiveTo(request.effectiveFrom());
            priceVersionRepository.save(latest);
        }
        MedicationPriceVersion created =
                new MedicationPriceVersion(UUID.randomUUID(), medication, request.price(), request.effectiveFrom());
        return PriceVersionDto.from(priceVersionRepository.save(created));
    }

    BigDecimal resolveCurrentPrice(UUID medicationId, LocalDate asOf) {
        return priceVersionRepository.findByMedicationIdOrderByEffectiveFromDesc(medicationId).stream()
                .filter(v -> !v.getEffectiveFrom().isAfter(asOf) && (v.getEffectiveTo() == null || v.getEffectiveTo().isAfter(asOf)))
                .map(MedicationPriceVersion::getPrice)
                .findFirst()
                .orElse(null);
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
