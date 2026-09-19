package mz.mva.pharmacy.service;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.DocumentStatus;
import mz.mva.pharmacy.domain.GoodsReceivedNote;
import mz.mva.pharmacy.domain.Medication;
import mz.mva.pharmacy.domain.PharmacyStore;
import mz.mva.pharmacy.domain.PharmacyTransactionType;
import mz.mva.pharmacy.domain.SupplierReturn;
import mz.mva.pharmacy.domain.SupplierReturnLine;
import mz.mva.pharmacy.dto.CreateSupplierReturnRequest;
import mz.mva.pharmacy.dto.SupplierReturnDto;
import mz.mva.pharmacy.dto.SupplierReturnLineDto;
import mz.mva.pharmacy.dto.SupplierReturnLineRequest;
import mz.mva.pharmacy.repository.SupplierReturnLineRepository;
import mz.mva.pharmacy.repository.SupplierReturnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Supplier Return (v9 §32) — linked to the original GRN whenever possible, never rewrites it.
 * Posting removes stock; {@link StockMovementService} rejects a return that would take a batch
 * negative, which is exactly the "return quantity must not exceed eligible available stock" rule.
 */
@Service
@Transactional
public class SupplierReturnService {

    private final SupplierReturnRepository returnRepository;
    private final SupplierReturnLineRepository lineRepository;
    private final PharmacyStoreService storeService;
    private final MedicationService medicationService;
    private final GrnService grnService;
    private final StockMovementService stockMovementService;
    private final SupplierReturnNumberGenerator numberGenerator;

    public SupplierReturnService(
            SupplierReturnRepository returnRepository,
            SupplierReturnLineRepository lineRepository,
            PharmacyStoreService storeService,
            MedicationService medicationService,
            GrnService grnService,
            StockMovementService stockMovementService,
            SupplierReturnNumberGenerator numberGenerator) {
        this.returnRepository = returnRepository;
        this.lineRepository = lineRepository;
        this.storeService = storeService;
        this.medicationService = medicationService;
        this.grnService = grnService;
        this.stockMovementService = stockMovementService;
        this.numberGenerator = numberGenerator;
    }

    public List<SupplierReturnDto> findAll() {
        return returnRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    public SupplierReturnDto findById(UUID id) {
        return toDto(getOrThrow(id));
    }

    public SupplierReturnDto create(CreateSupplierReturnRequest request) {
        PharmacyStore store = storeService.getOrThrow(request.storeId());
        GoodsReceivedNote originalGrn = request.originalGrnId() != null ? grnService.getOrThrow(request.originalGrnId()) : null;
        SupplierReturn supplierReturn = new SupplierReturn(
                UUID.randomUUID(), numberGenerator.next(), originalGrn, store, request.returnDate(), request.returnType(),
                request.reason(), request.createdByStaffId());
        SupplierReturn saved = returnRepository.save(supplierReturn);
        for (SupplierReturnLineRequest lineRequest : request.lines()) {
            Medication medication = medicationService.getOrThrow(lineRequest.medicationId());
            lineRepository.save(new SupplierReturnLine(
                    UUID.randomUUID(), saved, medication, lineRequest.batchNumber(), lineRequest.quantity()));
        }
        return toDto(saved);
    }

    public SupplierReturnDto post(UUID id, UUID staffId) {
        SupplierReturn supplierReturn = getOrThrow(id);
        if (supplierReturn.getStatus() == DocumentStatus.POSTED) {
            return toDto(supplierReturn);
        }
        if (supplierReturn.getStatus() == DocumentStatus.CANCELLED) {
            throw new InvalidStateTransitionException("Return " + supplierReturn.getReturnNumber() + " is cancelled, cannot post");
        }
        for (SupplierReturnLine line : lineRepository.findBySupplierReturnId(id)) {
            stockMovementService.apply(
                    line.getMedication(), supplierReturn.getStore(), line.getBatchNumber(), null, null, null,
                    -line.getQuantity(), PharmacyTransactionType.SUPPLIER_RETURN, "SUPPLIER_RETURN", supplierReturn.getId(),
                    supplierReturn.getReason(), staffId);
        }
        supplierReturn.post(staffId);
        return toDto(returnRepository.save(supplierReturn));
    }

    SupplierReturn getOrThrow(UUID id) {
        return returnRepository.findById(id).orElseThrow(() -> new NotFoundException("Supplier return not found: " + id));
    }

    private SupplierReturnDto toDto(SupplierReturn supplierReturn) {
        List<SupplierReturnLineDto> lines =
                lineRepository.findBySupplierReturnId(supplierReturn.getId()).stream().map(SupplierReturnLineDto::from).toList();
        return SupplierReturnDto.from(supplierReturn, lines);
    }
}
