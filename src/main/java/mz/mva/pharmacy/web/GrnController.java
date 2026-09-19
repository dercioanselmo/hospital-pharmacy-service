package mz.mva.pharmacy.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.dto.CreateGrnRequest;
import mz.mva.pharmacy.dto.GoodsReceivedNoteDto;
import mz.mva.pharmacy.dto.StaffActionRequest;
import mz.mva.pharmacy.service.GrnService;
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
@RequestMapping("/grns")
public class GrnController {

    private final GrnService grnService;

    public GrnController(GrnService grnService) {
        this.grnService = grnService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PHARMACY_STOCK_VIEW')")
    public List<GoodsReceivedNoteDto> findAll() {
        return grnService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PHARMACY_STOCK_VIEW')")
    public GoodsReceivedNoteDto findById(@PathVariable UUID id) {
        return grnService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PHARMACY_GRN_CREATE')")
    public GoodsReceivedNoteDto create(@Valid @RequestBody CreateGrnRequest request) {
        return grnService.create(request);
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('PHARMACY_GRN_POST')")
    public GoodsReceivedNoteDto post(@PathVariable UUID id, @Valid @RequestBody StaffActionRequest request) {
        return grnService.post(id, request.staffId());
    }
}
