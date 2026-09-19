package mz.mva.pharmacy.web;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.dto.PharmacyInventoryTransactionDto;
import mz.mva.pharmacy.repository.PharmacyInventoryTransactionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The inventory transaction history / ledger view (v9 §32/§80) — traceability back to the posting document. */
@RestController
@RequestMapping("/pharmacy-inventory-transactions")
public class PharmacyInventoryTransactionController {

    private final PharmacyInventoryTransactionRepository transactionRepository;

    public PharmacyInventoryTransactionController(PharmacyInventoryTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_STOCK_VIEW')")
    public List<PharmacyInventoryTransactionDto> search(@RequestParam(required = false) UUID medicationId) {
        if (medicationId != null) {
            return transactionRepository.findByMedicationIdOrderByCreatedAtDesc(medicationId).stream()
                    .map(PharmacyInventoryTransactionDto::from)
                    .toList();
        }
        return transactionRepository.findAll().stream().map(PharmacyInventoryTransactionDto::from).toList();
    }
}
