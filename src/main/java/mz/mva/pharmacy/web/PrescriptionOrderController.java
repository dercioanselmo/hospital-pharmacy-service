package mz.mva.pharmacy.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.PrescriptionOrderStatus;
import mz.mva.pharmacy.dto.PrescriptionOrderDto;
import mz.mva.pharmacy.dto.ReceiveOrderRequest;
import mz.mva.pharmacy.dto.VerifyOrderRequest;
import mz.mva.pharmacy.service.PatientOwnershipGuard;
import mz.mva.pharmacy.service.PrescriptionOrderService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prescription-orders")
public class PrescriptionOrderController {

    private final PrescriptionOrderService prescriptionOrderService;
    private final PatientOwnershipGuard patientOwnershipGuard;

    public PrescriptionOrderController(
            PrescriptionOrderService prescriptionOrderService, PatientOwnershipGuard patientOwnershipGuard) {
        this.prescriptionOrderService = prescriptionOrderService;
        this.patientOwnershipGuard = patientOwnershipGuard;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_ORDER_VIEW')")
    public List<PrescriptionOrderDto> queue(@RequestParam(required = false) PrescriptionOrderStatus status) {
        return prescriptionOrderService.findQueue(status);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAuthority('PHARMACY_ORDER_VIEW')")
    public List<PrescriptionOrderDto> findByPatient(
            @PathVariable UUID patientId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            Authentication authentication) {
        patientOwnershipGuard.enforce(authorization, authentication.getName(), patientId);
        return prescriptionOrderService.findByPatient(patientId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PHARMACY_ORDER_VIEW')")
    public PrescriptionOrderDto findById(@PathVariable UUID id) {
        return prescriptionOrderService.findById(id);
    }

    @PostMapping("/receive")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PHARMACY_ORDER_RECEIVE')")
    public PrescriptionOrderDto receive(
            @Valid @RequestBody ReceiveOrderRequest request, @RequestHeader("Authorization") String authHeader) {
        return prescriptionOrderService.receive(request, bearerToken(authHeader));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('PHARMACY_ORDER_VERIFY')")
    public PrescriptionOrderDto verify(
            @PathVariable UUID id,
            @Valid @RequestBody VerifyOrderRequest request,
            @RequestHeader("Authorization") String authHeader) {
        return prescriptionOrderService.verify(id, request.staffId(), bearerToken(authHeader));
    }

    @PostMapping("/{id}/start-dispensing")
    @PreAuthorize("hasAuthority('PHARMACY_DISPENSE')")
    public PrescriptionOrderDto startDispensing(
            @PathVariable UUID id, @RequestHeader("Authorization") String authHeader) {
        return prescriptionOrderService.startDispensing(id, bearerToken(authHeader));
    }

    private String bearerToken(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
    }
}
