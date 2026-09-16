package mz.mva.pharmacy.client;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Calls hospital-clinical-service directly (Eureka discovery, bypassing the
 * gateway) to keep ClinicalOrder status in sync as Pharmacy fulfills it —
 * same pattern as Laboratory (Phase 4a) and Radiology (Phase 4b). Clinical
 * owns order routing/status; Pharmacy owns fulfillment detail (verification,
 * dispensing) — see docs/service-boundaries.md.
 *
 * <p>The caller's own bearer token is forwarded so Clinical's permission
 * checks (CLINICAL_ORDER_FULFILL) apply to the actual user, not a shared
 * service credential — there is no service-to-service auth scheme in this
 * platform yet.
 */
@Component
public class ClinicalOrderClient {

    private static final String BASE_URL = "http://clinical-service";

    private final RestTemplate restTemplate;

    public ClinicalOrderClient(RestTemplate loadBalancedRestTemplate) {
        this.restTemplate = loadBalancedRestTemplate;
    }

    public ClinicalOrderView fetch(UUID clinicalOrderId, String bearerToken) {
        try {
            return restTemplate
                    .exchange(
                            BASE_URL + "/orders/{id}",
                            org.springframework.http.HttpMethod.GET,
                            new HttpEntity<>(authHeaders(bearerToken)),
                            ClinicalOrderView.class,
                            clinicalOrderId)
                    .getBody();
        } catch (RestClientException e) {
            throw new ClinicalServiceException("Failed to fetch clinical order " + clinicalOrderId, e);
        }
    }

    public void claim(UUID clinicalOrderId, UUID staffId, String bearerToken) {
        post(clinicalOrderId, "/claim", Map.of("staffId", staffId), bearerToken);
    }

    public void ready(UUID clinicalOrderId, String bearerToken) {
        post(clinicalOrderId, "/ready", null, bearerToken);
    }

    public void start(UUID clinicalOrderId, String bearerToken) {
        post(clinicalOrderId, "/start", null, bearerToken);
    }

    public void complete(UUID clinicalOrderId, String bearerToken) {
        post(clinicalOrderId, "/complete", null, bearerToken);
    }

    private void post(UUID clinicalOrderId, String action, Object body, String bearerToken) {
        try {
            restTemplate.postForEntity(
                    BASE_URL + "/orders/{id}" + action, new HttpEntity<>(body, authHeaders(bearerToken)), Void.class, clinicalOrderId);
        } catch (RestClientException e) {
            throw new ClinicalServiceException(
                    "Failed to " + action.replace("/", "") + " clinical order " + clinicalOrderId, e);
        }
    }

    private HttpHeaders authHeaders(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        if (bearerToken != null) {
            headers.setBearerAuth(bearerToken);
        }
        return headers;
    }
}
