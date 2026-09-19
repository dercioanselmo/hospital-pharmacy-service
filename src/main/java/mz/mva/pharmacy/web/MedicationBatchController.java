package mz.mva.pharmacy.web;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.dto.MedicationBatchDto;
import mz.mva.pharmacy.service.MedicationBatchService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Item-wise (by medication) and batch-wise (by store) stock views (v9 §24/§32). */
@RestController
@RequestMapping("/medication-batches")
public class MedicationBatchController {

    private final MedicationBatchService batchService;

    public MedicationBatchController(MedicationBatchService batchService) {
        this.batchService = batchService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_STOCK_VIEW')")
    public List<MedicationBatchDto> search(
            @RequestParam(required = false) UUID medicationId, @RequestParam(required = false) UUID storeId) {
        if (medicationId != null) {
            return batchService.findByMedication(medicationId);
        }
        if (storeId != null) {
            return batchService.findByStore(storeId);
        }
        return batchService.findAll();
    }
}
