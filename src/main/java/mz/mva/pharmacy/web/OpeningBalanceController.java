package mz.mva.pharmacy.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.dto.CreateOpeningBalanceRequest;
import mz.mva.pharmacy.dto.OpeningBalanceDto;
import mz.mva.pharmacy.dto.StaffActionRequest;
import mz.mva.pharmacy.service.OpeningBalanceService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Elevated authorization throughout (v9 §32) — both create and post require PHARMACY_OPENING_BALANCE. */
@RestController
@RequestMapping("/opening-balances")
public class OpeningBalanceController {

    private final OpeningBalanceService openingBalanceService;

    public OpeningBalanceController(OpeningBalanceService openingBalanceService) {
        this.openingBalanceService = openingBalanceService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_STOCK_VIEW')")
    public List<OpeningBalanceDto> findAll() {
        return openingBalanceService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PHARMACY_OPENING_BALANCE')")
    public OpeningBalanceDto create(@Valid @RequestBody CreateOpeningBalanceRequest request) {
        return openingBalanceService.create(request);
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('PHARMACY_OPENING_BALANCE')")
    public OpeningBalanceDto post(@PathVariable UUID id, @Valid @RequestBody StaffActionRequest request) {
        return openingBalanceService.post(id, request.staffId());
    }
}
