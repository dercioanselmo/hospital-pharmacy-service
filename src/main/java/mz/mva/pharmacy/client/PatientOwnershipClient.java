package mz.mva.pharmacy.client;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Row-level-security check (flagged since Phase 9g): resolves whether the
 * caller's own identity {@code userId} corresponds to a {@code Patient}
 * record, by calling hospital-patient-service directly (Eureka discovery,
 * bypassing the gateway) with the caller's own forwarded bearer token — same
 * pattern as this service's own {@link ClinicalOrderClient} (Phase 5a). A
 * failed/empty lookup means "not a patient-linked caller" (the normal staff
 * path), never an error.
 */
@Component
public class PatientOwnershipClient {

    private static final Logger log = LoggerFactory.getLogger(PatientOwnershipClient.class);
    private static final String BASE_URL = "http://patient-service";

    private final RestTemplate restTemplate;

    public PatientOwnershipClient(RestTemplate loadBalancedRestTemplate) {
        this.restTemplate = loadBalancedRestTemplate;
    }

    public Optional<UUID> findOwnPatientId(String bearerToken, UUID subjectUserId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
            PatientView[] views = restTemplate
                    .exchange(
                            BASE_URL + "/patients?userId={userId}",
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            PatientView[].class,
                            subjectUserId)
                    .getBody();
            return views != null && views.length > 0 ? Optional.of(views[0].id()) : Optional.empty();
        } catch (RestClientException e) {
            log.warn("Failed to resolve own patient record for userId {} during ownership check", subjectUserId, e);
            return Optional.empty();
        }
    }
}
