package mz.mva.pharmacy.service;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.DocumentStatus;
import mz.mva.pharmacy.domain.GoodsReceivedNote;
import mz.mva.pharmacy.domain.GrnLine;
import mz.mva.pharmacy.domain.Medication;
import mz.mva.pharmacy.domain.PharmacyStore;
import mz.mva.pharmacy.domain.PharmacyTransactionType;
import mz.mva.pharmacy.dto.CreateGrnRequest;
import mz.mva.pharmacy.dto.GoodsReceivedNoteDto;
import mz.mva.pharmacy.dto.GrnLineDto;
import mz.mva.pharmacy.dto.GrnLineRequest;
import mz.mva.pharmacy.repository.GoodsReceivedNoteRepository;
import mz.mva.pharmacy.repository.GrnLineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Bulk GRN (v9 §24/§32) — save never changes stock; only {@link #post} does, and idempotently. */
@Service
@Transactional
public class GrnService {

    private final GoodsReceivedNoteRepository grnRepository;
    private final GrnLineRepository lineRepository;
    private final PharmacyStoreService storeService;
    private final MedicationService medicationService;
    private final StockMovementService stockMovementService;
    private final GrnNumberGenerator numberGenerator;

    public GrnService(
            GoodsReceivedNoteRepository grnRepository,
            GrnLineRepository lineRepository,
            PharmacyStoreService storeService,
            MedicationService medicationService,
            StockMovementService stockMovementService,
            GrnNumberGenerator numberGenerator) {
        this.grnRepository = grnRepository;
        this.lineRepository = lineRepository;
        this.storeService = storeService;
        this.medicationService = medicationService;
        this.stockMovementService = stockMovementService;
        this.numberGenerator = numberGenerator;
    }

    public List<GoodsReceivedNoteDto> findAll() {
        return grnRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    public GoodsReceivedNoteDto findById(UUID id) {
        return toDto(getOrThrow(id));
    }

    public GoodsReceivedNoteDto create(CreateGrnRequest request) {
        PharmacyStore store = storeService.getOrThrow(request.storeId());
        GoodsReceivedNote grn = new GoodsReceivedNote(
                UUID.randomUUID(), numberGenerator.next(), store, request.supplierName(), request.receiptDate(),
                request.createdByStaffId());
        GoodsReceivedNote saved = grnRepository.save(grn);
        for (GrnLineRequest lineRequest : request.lines()) {
            Medication medication = medicationService.getOrThrow(lineRequest.medicationId());
            GrnLine line = new GrnLine(
                    UUID.randomUUID(), saved, medication, lineRequest.batchNumber(), lineRequest.expiryDate(),
                    lineRequest.quantity(), lineRequest.freeQuantity(), lineRequest.costPrice(), lineRequest.salePrice());
            lineRepository.save(line);
        }
        return toDto(saved);
    }

    /** Posting is idempotent — calling it again on an already-POSTED GRN is a silent no-op, never a second stock increase. */
    public GoodsReceivedNoteDto post(UUID id, UUID staffId) {
        GoodsReceivedNote grn = getOrThrow(id);
        if (grn.getStatus() == DocumentStatus.POSTED) {
            return toDto(grn);
        }
        if (grn.getStatus() == DocumentStatus.CANCELLED) {
            throw new InvalidStateTransitionException("GRN " + grn.getGrnNumber() + " is cancelled, cannot post");
        }
        for (GrnLine line : lineRepository.findByGrnId(id)) {
            stockMovementService.apply(
                    line.getMedication(), grn.getStore(), line.getBatchNumber(), line.getExpiryDate(), line.getCostPrice(),
                    line.getSalePrice(), line.totalReceivedQuantity(), PharmacyTransactionType.GRN_RECEIPT, "GRN", grn.getId(),
                    "GRN " + grn.getGrnNumber() + " from " + grn.getSupplierName(), staffId);
        }
        grn.post(staffId);
        return toDto(grnRepository.save(grn));
    }

    GoodsReceivedNote getOrThrow(UUID id) {
        return grnRepository.findById(id).orElseThrow(() -> new NotFoundException("GRN not found: " + id));
    }

    private GoodsReceivedNoteDto toDto(GoodsReceivedNote grn) {
        List<GrnLineDto> lines = lineRepository.findByGrnId(grn.getId()).stream().map(GrnLineDto::from).toList();
        return GoodsReceivedNoteDto.from(grn, lines);
    }
}
