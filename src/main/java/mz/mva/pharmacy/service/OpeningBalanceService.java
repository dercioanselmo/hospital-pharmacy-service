package mz.mva.pharmacy.service;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.DocumentStatus;
import mz.mva.pharmacy.domain.Medication;
import mz.mva.pharmacy.domain.OpeningBalance;
import mz.mva.pharmacy.domain.PharmacyStore;
import mz.mva.pharmacy.domain.PharmacyTransactionType;
import mz.mva.pharmacy.dto.CreateOpeningBalanceRequest;
import mz.mva.pharmacy.dto.OpeningBalanceDto;
import mz.mva.pharmacy.repository.OpeningBalanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Opening Balance (v9 §32) — initial/go-live stock, distinct from a normal supplier receipt.
 * Elevated authorization is enforced at the controller (a dedicated permission, not the general
 * GRN-create one) — this service just posts the transaction with its own dedicated type.
 */
@Service
@Transactional
public class OpeningBalanceService {

    private final OpeningBalanceRepository balanceRepository;
    private final PharmacyStoreService storeService;
    private final MedicationService medicationService;
    private final StockMovementService stockMovementService;
    private final OpeningBalanceNumberGenerator numberGenerator;

    public OpeningBalanceService(
            OpeningBalanceRepository balanceRepository,
            PharmacyStoreService storeService,
            MedicationService medicationService,
            StockMovementService stockMovementService,
            OpeningBalanceNumberGenerator numberGenerator) {
        this.balanceRepository = balanceRepository;
        this.storeService = storeService;
        this.medicationService = medicationService;
        this.stockMovementService = stockMovementService;
        this.numberGenerator = numberGenerator;
    }

    public List<OpeningBalanceDto> findAll() {
        return balanceRepository.findAllByOrderByCreatedAtDesc().stream().map(OpeningBalanceDto::from).toList();
    }

    public OpeningBalanceDto create(CreateOpeningBalanceRequest request) {
        PharmacyStore store = storeService.getOrThrow(request.storeId());
        Medication medication = medicationService.getOrThrow(request.medicationId());
        OpeningBalance balance = new OpeningBalance(
                UUID.randomUUID(), numberGenerator.next(), store, medication, request.batchNumber(), request.quantity(),
                request.createdByStaffId());
        balance.setExpiryDate(request.expiryDate());
        balance.setCostPrice(request.costPrice());
        balance.setReason(request.reason());
        return OpeningBalanceDto.from(balanceRepository.save(balance));
    }

    public OpeningBalanceDto post(UUID id, UUID staffId) {
        OpeningBalance balance = getOrThrow(id);
        if (balance.getStatus() == DocumentStatus.POSTED) {
            return OpeningBalanceDto.from(balance);
        }
        if (balance.getStatus() == DocumentStatus.CANCELLED) {
            throw new InvalidStateTransitionException(
                    "Opening balance " + balance.getBalanceNumber() + " is cancelled, cannot post");
        }
        stockMovementService.apply(
                balance.getMedication(), balance.getStore(), balance.getBatchNumber(), balance.getExpiryDate(),
                balance.getCostPrice(), null, balance.getQuantity(), PharmacyTransactionType.OPENING_BALANCE,
                "OPENING_BALANCE", balance.getId(), balance.getReason(), staffId);
        balance.post(staffId);
        return OpeningBalanceDto.from(balanceRepository.save(balance));
    }

    private OpeningBalance getOrThrow(UUID id) {
        return balanceRepository.findById(id).orElseThrow(() -> new NotFoundException("Opening balance not found: " + id));
    }
}
