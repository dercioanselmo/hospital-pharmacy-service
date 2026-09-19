package mz.mva.pharmacy.service;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.PharmacyStore;
import mz.mva.pharmacy.dto.PharmacyStoreDto;
import mz.mva.pharmacy.repository.PharmacyStoreRepository;
import org.springframework.stereotype.Service;

@Service
public class PharmacyStoreService {

    private final PharmacyStoreRepository storeRepository;

    public PharmacyStoreService(PharmacyStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<PharmacyStoreDto> findAll() {
        return storeRepository.findAll().stream().map(PharmacyStoreDto::from).toList();
    }

    public PharmacyStoreDto create(PharmacyStoreDto dto) {
        PharmacyStore store = new PharmacyStore(UUID.randomUUID(), dto.code(), dto.name());
        return PharmacyStoreDto.from(storeRepository.save(store));
    }

    PharmacyStore getOrThrow(UUID id) {
        return storeRepository.findById(id).orElseThrow(() -> new NotFoundException("Pharmacy store not found: " + id));
    }
}
