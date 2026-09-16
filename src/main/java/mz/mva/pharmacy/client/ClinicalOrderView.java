package mz.mva.pharmacy.client;

import java.util.UUID;

/** Mirrors hospital-clinical-service's ClinicalOrderDto shape — only the fields this service needs. */
public record ClinicalOrderView(
        UUID id,
        UUID encounterId,
        UUID patientId,
        String orderType,
        UUID orderingStaffId,
        UUID targetDepartmentId,
        UUID assignedStaffId,
        String details,
        String status) {
}
