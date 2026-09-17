package mz.mva.pharmacy.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

/** Mirrors the one field this client needs from hospital-patient-service's PatientDto. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PatientView(UUID id) {
}
