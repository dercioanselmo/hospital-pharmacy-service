package mz.mva.pharmacy.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.dto.CreateSupplierReturnRequest;
import mz.mva.pharmacy.dto.StaffActionRequest;
import mz.mva.pharmacy.dto.SupplierReturnDto;
import mz.mva.pharmacy.service.SupplierReturnService;
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
@RequestMapping("/supplier-returns")
public class SupplierReturnController {

    private final SupplierReturnService supplierReturnService;

    public SupplierReturnController(SupplierReturnService supplierReturnService) {
        this.supplierReturnService = supplierReturnService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_STOCK_VIEW')")
    public List<SupplierReturnDto> findAll() {
        return supplierReturnService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PHARMACY_STOCK_VIEW')")
    public SupplierReturnDto findById(@PathVariable UUID id) {
        return supplierReturnService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PHARMACY_RETURN_CREATE')")
    public SupplierReturnDto create(@Valid @RequestBody CreateSupplierReturnRequest request) {
        return supplierReturnService.create(request);
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('PHARMACY_RETURN_POST')")
    public SupplierReturnDto post(@PathVariable UUID id, @Valid @RequestBody StaffActionRequest request) {
        return supplierReturnService.post(id, request.staffId());
    }
}
