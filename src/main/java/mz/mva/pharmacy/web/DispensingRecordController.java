package mz.mva.pharmacy.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.dto.CompleteDispensingRequest;
import mz.mva.pharmacy.dto.DispenseRequest;
import mz.mva.pharmacy.dto.DispensingRecordDto;
import mz.mva.pharmacy.service.DispensingRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DispensingRecordController {

    private final DispensingRecordService dispensingRecordService;

    public DispensingRecordController(DispensingRecordService dispensingRecordService) {
        this.dispensingRecordService = dispensingRecordService;
    }

    @GetMapping("/prescription-orders/{prescriptionOrderId}/dispensing-records")
    @PreAuthorize("hasAuthority('PHARMACY_ORDER_VIEW')")
    public List<DispensingRecordDto> findByPrescriptionOrder(@PathVariable UUID prescriptionOrderId) {
        return dispensingRecordService.findByPrescriptionOrder(prescriptionOrderId);
    }

    @PostMapping("/prescription-orders/{prescriptionOrderId}/dispensing-records")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PHARMACY_DISPENSE')")
    public DispensingRecordDto create(
            @PathVariable UUID prescriptionOrderId, @Valid @RequestBody DispenseRequest request) {
        return dispensingRecordService.create(prescriptionOrderId, request.quantityDispensed(), request.staffId());
    }

    @PostMapping("/dispensing-records/{id}/complete")
    @PreAuthorize("hasAuthority('PHARMACY_DISPENSE_COMPLETE')")
    public DispensingRecordDto complete(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteDispensingRequest request,
            @RequestHeader("Authorization") String authHeader) {
        return dispensingRecordService.complete(id, request.staffId(), bearerToken(authHeader));
    }

    private String bearerToken(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
    }
}
