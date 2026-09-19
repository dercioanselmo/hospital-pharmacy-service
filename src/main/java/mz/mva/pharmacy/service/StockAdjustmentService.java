package mz.mva.pharmacy.service;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.AdjustmentDirection;
import mz.mva.pharmacy.domain.AdjustmentStatus;
import mz.mva.pharmacy.domain.Medication;
import mz.mva.pharmacy.domain.PharmacyStore;
import mz.mva.pharmacy.domain.PharmacyTransactionType;
import mz.mva.pharmacy.domain.StockAdjustment;
import mz.mva.pharmacy.dto.CreateStockAdjustmentRequest;
import mz.mva.pharmacy.dto.StockAdjustmentDto;
import mz.mva.pharmacy.repository.MedicationBatchRepository;
import mz.mva.pharmacy.repository.StockAdjustmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stock Adjustment (v9 §32) — physical-vs-system reconciliation. Requires an explicit approval
 * step before posting: "ordinary pharmacy users must not be able to freely increase/decrease
 * stock without the appropriate permission" — approve and post are gated on different permissions
 * at the controller.
 */
@Service
@Transactional
public class StockAdjustmentService {

    private final StockAdjustmentRepository adjustmentRepository;
    private final MedicationBatchRepository batchRepository;
    private final PharmacyStoreService storeService;
    private final MedicationService medicationService;
    private final StockMovementService stockMovementService;
    private final StockAdjustmentNumberGenerator numberGenerator;

    public StockAdjustmentService(
            StockAdjustmentRepository adjustmentRepository,
            MedicationBatchRepository batchRepository,
            PharmacyStoreService storeService,
            MedicationService medicationService,
            StockMovementService stockMovementService,
            StockAdjustmentNumberGenerator numberGenerator) {
        this.adjustmentRepository = adjustmentRepository;
        this.batchRepository = batchRepository;
        this.storeService = storeService;
        this.medicationService = medicationService;
        this.stockMovementService = stockMovementService;
        this.numberGenerator = numberGenerator;
    }

    public List<StockAdjustmentDto> findAll() {
        return adjustmentRepository.findAllByOrderByCreatedAtDesc().stream().map(StockAdjustmentDto::from).toList();
    }

    public StockAdjustmentDto create(CreateStockAdjustmentRequest request) {
        PharmacyStore store = storeService.getOrThrow(request.storeId());
        Medication medication = medicationService.getOrThrow(request.medicationId());
        StockAdjustment adjustment = new StockAdjustment(
                UUID.randomUUID(), numberGenerator.next(), store, medication, request.batchNumber(), request.direction(),
                request.quantity(), request.reason(), request.createdByStaffId());
        return StockAdjustmentDto.from(adjustmentRepository.save(adjustment));
    }

    public StockAdjustmentDto approve(UUID id, UUID staffId) {
        StockAdjustment adjustment = getOrThrow(id);
        if (adjustment.getStatus() != AdjustmentStatus.DRAFT) {
            throw new InvalidStateTransitionException(
                    "Adjustment " + adjustment.getAdjustmentNumber() + " is " + adjustment.getStatus() + ", can only approve a DRAFT one");
        }
        adjustment.approve(staffId);
        return StockAdjustmentDto.from(adjustmentRepository.save(adjustment));
    }

    public StockAdjustmentDto post(UUID id, UUID staffId) {
        StockAdjustment adjustment = getOrThrow(id);
        if (adjustment.getStatus() == AdjustmentStatus.POSTED) {
            return StockAdjustmentDto.from(adjustment);
        }
        if (adjustment.getStatus() != AdjustmentStatus.APPROVED) {
            throw new InvalidStateTransitionException(
                    "Adjustment " + adjustment.getAdjustmentNumber() + " must be APPROVED before posting");
        }
        int before = batchRepository
                .findByMedicationIdAndStoreIdAndBatchNumber(
                        adjustment.getMedication().getId(), adjustment.getStore().getId(), adjustment.getBatchNumber())
                .map(b -> b.getQuantityAvailable())
                .orElse(0);
        int delta = adjustment.getDirection() == AdjustmentDirection.RECEIVE ? adjustment.getQuantity() : -adjustment.getQuantity();
        PharmacyTransactionType type = adjustment.getDirection() == AdjustmentDirection.RECEIVE
                ? PharmacyTransactionType.STOCK_ADJUSTMENT_RECEIVE
                : PharmacyTransactionType.STOCK_ADJUSTMENT_ISSUE;
        stockMovementService.apply(
                adjustment.getMedication(), adjustment.getStore(), adjustment.getBatchNumber(), null, null, null, delta, type,
                "STOCK_ADJUSTMENT", adjustment.getId(), adjustment.getReason(), staffId);
        int after = before + delta;
        adjustment.post(staffId, before, after);
        return StockAdjustmentDto.from(adjustmentRepository.save(adjustment));
    }

    private StockAdjustment getOrThrow(UUID id) {
        return adjustmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Stock adjustment not found: " + id));
    }
}
