package mz.mva.pharmacy.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.dto.CreateStockAdjustmentRequest;
import mz.mva.pharmacy.dto.StaffActionRequest;
import mz.mva.pharmacy.dto.StockAdjustmentDto;
import mz.mva.pharmacy.service.StockAdjustmentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock-adjustments")
public class StockAdjustmentController {

    private final StockAdjustmentService adjustmentService;

    public StockAdjustmentController(StockAdjustmentService adjustmentService) {
        this.adjustmentService = adjustmentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_STOCK_VIEW')")
    public List<StockAdjustmentDto> findAll() {
        return adjustmentService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PHARMACY_ADJUSTMENT_CREATE')")
    public StockAdjustmentDto create(@Valid @RequestBody CreateStockAdjustmentRequest request) {
        return adjustmentService.create(request);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('PHARMACY_ADJUSTMENT_APPROVE')")
    public StockAdjustmentDto approve(@PathVariable UUID id, @Valid @RequestBody StaffActionRequest request) {
        return adjustmentService.approve(id, request.staffId());
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('PHARMACY_ADJUSTMENT_APPROVE')")
    public StockAdjustmentDto post(@PathVariable UUID id, @Valid @RequestBody StaffActionRequest request) {
        return adjustmentService.post(id, request.staffId());
    }
}
