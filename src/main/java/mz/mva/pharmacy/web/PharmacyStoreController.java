package mz.mva.pharmacy.web;

import jakarta.validation.Valid;
import java.util.List;
import mz.mva.pharmacy.dto.PharmacyStoreDto;
import mz.mva.pharmacy.service.PharmacyStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pharmacy-stores")
public class PharmacyStoreController {

    private final PharmacyStoreService storeService;

    public PharmacyStoreController(PharmacyStoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_STOCK_VIEW')")
    public List<PharmacyStoreDto> findAll() {
        return storeService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PHARMACY_STORE_MANAGE')")
    public PharmacyStoreDto create(@Valid @RequestBody PharmacyStoreDto dto) {
        return storeService.create(dto);
    }
}
