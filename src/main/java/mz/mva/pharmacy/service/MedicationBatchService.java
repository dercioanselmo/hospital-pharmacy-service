package mz.mva.pharmacy.service;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.dto.MedicationBatchDto;
import mz.mva.pharmacy.repository.MedicationBatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Item-wise and batch-wise stock views (v9 §24/§32) — derived directly from the authoritative {@link mz.mva.pharmacy.domain.MedicationBatch} records. */
@Service
@Transactional(readOnly = true)
public class MedicationBatchService {

    private final MedicationBatchRepository batchRepository;

    public MedicationBatchService(MedicationBatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    public List<MedicationBatchDto> findByMedication(UUID medicationId) {
        return batchRepository.findByMedicationIdOrderByExpiryDateAsc(medicationId).stream()
                .map(MedicationBatchDto::from)
                .toList();
    }

    public List<MedicationBatchDto> findByStore(UUID storeId) {
        return batchRepository.findByStoreIdOrderByExpiryDateAsc(storeId).stream().map(MedicationBatchDto::from).toList();
    }

    public List<MedicationBatchDto> findAll() {
        return batchRepository.findAll().stream().map(MedicationBatchDto::from).toList();
    }
}
