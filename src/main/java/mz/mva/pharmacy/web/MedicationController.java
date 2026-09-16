package mz.mva.pharmacy.web;

import jakarta.validation.Valid;
import java.util.List;
import mz.mva.pharmacy.dto.MedicationDto;
import mz.mva.pharmacy.service.MedicationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medications")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_ORDER_VIEW')")
    public List<MedicationDto> findAll() {
        return medicationService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PHARMACY_MEDICATION_MANAGE')")
    public MedicationDto create(@Valid @RequestBody MedicationDto dto) {
        return medicationService.create(dto);
    }
}
