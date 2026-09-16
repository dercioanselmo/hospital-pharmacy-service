package mz.mva.pharmacy.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import mz.mva.pharmacy.client.ClinicalOrderClient;
import mz.mva.pharmacy.client.ClinicalOrderView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Covers the full receive -> verify -> start-dispensing -> dispense -> complete
 * pipeline, plus the insufficient-stock business rule. ClinicalOrderClient is
 * mocked — this suite never makes a real network call to hospital-clinical-service.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PrescriptionOrderWorkflowIntegrationTest {

    private static final String JWT_SECRET = "test-secret-key-at-least-32-bytes-long-for-hmac";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("mva.security.jwt.secret", () -> JWT_SECRET);
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("eureka.client.register-with-eureka", () -> "false");
        registry.add("eureka.client.fetch-registry", () -> "false");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ClinicalOrderClient clinicalOrderClient;

    private HttpHeaders authHeaders(List<String> permissions) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
        String token = Jwts.builder()
                .subject("test-user")
                .claim("permissions", permissions)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    @SuppressWarnings("unchecked")
    private String seededMedicationId(String code) {
        ResponseEntity<List> medications = restTemplate.exchange(
                "/medications", org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(authHeaders(List.of("PHARMACY_ORDER_VIEW"))), List.class);
        return ((List<Map<String, Object>>) medications.getBody())
                .stream()
                .filter(m -> code.equals(m.get("code")))
                .map(m -> (String) m.get("id"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void fullPipelineFromReceiveToCompleteSyncsClinicalOrderAtEachStep() {
        UUID clinicalOrderId = UUID.randomUUID();
        UUID staffId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        when(clinicalOrderClient.fetch(eq(clinicalOrderId), any())).thenReturn(new ClinicalOrderView(
                clinicalOrderId, encounterId, patientId, "PRESCRIPTION", staffId, UUID.randomUUID(), null,
                "Paracetamol 500mg", "PLACED"));

        String medicationId = seededMedicationId("PARA-500");

        String receiveBody = String.format(
                "{\"clinicalOrderId\":\"%s\",\"staffId\":\"%s\",\"medicationId\":\"%s\",\"quantityPrescribed\":20,\"dosageInstructions\":\"1 comprimido de 8/8h\"}",
                clinicalOrderId, staffId, medicationId);
        ResponseEntity<Map> received = restTemplate.postForEntity(
                "/prescription-orders/receive",
                new HttpEntity<>(receiveBody, authHeaders(List.of("PHARMACY_ORDER_RECEIVE"))),
                Map.class);
        assertThat(received.getStatusCode().value()).isEqualTo(201);
        assertThat(received.getBody().get("status")).isEqualTo("RECEIVED");
        String prescriptionOrderId = (String) received.getBody().get("id");
        verify(clinicalOrderClient).claim(eq(clinicalOrderId), eq(staffId), any());

        ResponseEntity<Map> verified = restTemplate.postForEntity(
                "/prescription-orders/" + prescriptionOrderId + "/verify",
                new HttpEntity<>(
                        String.format("{\"staffId\":\"%s\"}", staffId),
                        authHeaders(List.of("PHARMACY_ORDER_VERIFY"))),
                Map.class);
        assertThat(verified.getBody().get("status")).isEqualTo("VERIFIED");
        verify(clinicalOrderClient).ready(eq(clinicalOrderId), any());

        ResponseEntity<Map> dispensing = restTemplate.postForEntity(
                "/prescription-orders/" + prescriptionOrderId + "/start-dispensing",
                new HttpEntity<>(authHeaders(List.of("PHARMACY_DISPENSE"))),
                Map.class);
        assertThat(dispensing.getBody().get("status")).isEqualTo("DISPENSING");
        verify(clinicalOrderClient).start(eq(clinicalOrderId), any());

        ResponseEntity<Map> dispensed = restTemplate.postForEntity(
                "/prescription-orders/" + prescriptionOrderId + "/dispensing-records",
                new HttpEntity<>(
                        String.format("{\"quantityDispensed\":20,\"staffId\":\"%s\"}", staffId),
                        authHeaders(List.of("PHARMACY_DISPENSE"))),
                Map.class);
        assertThat(dispensed.getStatusCode().value()).isEqualTo(201);
        String dispensingRecordId = (String) dispensed.getBody().get("id");

        ResponseEntity<Map> orderAfterDispense = restTemplate.exchange(
                "/prescription-orders/" + prescriptionOrderId, org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(authHeaders(List.of("PHARMACY_ORDER_VIEW"))), Map.class);
        assertThat(orderAfterDispense.getBody().get("status")).isEqualTo("DISPENSED");

        ResponseEntity<Map> completed = restTemplate.postForEntity(
                "/dispensing-records/" + dispensingRecordId + "/complete",
                new HttpEntity<>(
                        String.format("{\"staffId\":\"%s\"}", staffId),
                        authHeaders(List.of("PHARMACY_DISPENSE_COMPLETE"))),
                Map.class);
        assertThat(completed.getStatusCode().value()).isEqualTo(200);
        verify(clinicalOrderClient).complete(eq(clinicalOrderId), any());

        ResponseEntity<Map> finalOrder = restTemplate.exchange(
                "/prescription-orders/" + prescriptionOrderId, org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(authHeaders(List.of("PHARMACY_ORDER_VIEW"))), Map.class);
        assertThat(finalOrder.getBody().get("status")).isEqualTo("COMPLETED");
    }

    @Test
    void dispensingMoreThanOnHandFails() {
        UUID clinicalOrderId = UUID.randomUUID();
        UUID staffId = UUID.randomUUID();
        when(clinicalOrderClient.fetch(eq(clinicalOrderId), any())).thenReturn(new ClinicalOrderView(
                clinicalOrderId, UUID.randomUUID(), UUID.randomUUID(), "PRESCRIPTION", staffId, UUID.randomUUID(),
                null, "Amoxicilina 250mg", "PLACED"));

        String medicationId = seededMedicationId("AMOX-250");

        String receiveBody = String.format(
                "{\"clinicalOrderId\":\"%s\",\"staffId\":\"%s\",\"medicationId\":\"%s\",\"quantityPrescribed\":10,\"dosageInstructions\":\"...\"}",
                clinicalOrderId, staffId, medicationId);
        ResponseEntity<Map> received = restTemplate.postForEntity(
                "/prescription-orders/receive",
                new HttpEntity<>(receiveBody, authHeaders(List.of("PHARMACY_ORDER_RECEIVE"))),
                Map.class);
        String prescriptionOrderId = (String) received.getBody().get("id");

        restTemplate.postForEntity(
                "/prescription-orders/" + prescriptionOrderId + "/verify",
                new HttpEntity<>(
                        String.format("{\"staffId\":\"%s\"}", staffId),
                        authHeaders(List.of("PHARMACY_ORDER_VERIFY"))),
                Map.class);
        restTemplate.postForEntity(
                "/prescription-orders/" + prescriptionOrderId + "/start-dispensing",
                new HttpEntity<>(authHeaders(List.of("PHARMACY_DISPENSE"))),
                Map.class);

        ResponseEntity<Map> overDispense = restTemplate.postForEntity(
                "/prescription-orders/" + prescriptionOrderId + "/dispensing-records",
                new HttpEntity<>(
                        String.format("{\"quantityDispensed\":999999,\"staffId\":\"%s\"}", staffId),
                        authHeaders(List.of("PHARMACY_DISPENSE"))),
                Map.class);
        assertThat(overDispense.getStatusCode().value()).isEqualTo(409);
        assertThat(overDispense.getBody().get("error")).isEqualTo("INSUFFICIENT_STOCK");
    }

    @Test
    void receivingRequiresPermission() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/prescription-orders/receive",
                new HttpEntity<>(
                        "{\"clinicalOrderId\":\"" + UUID.randomUUID() + "\",\"staffId\":\"" + UUID.randomUUID()
                                + "\",\"medicationId\":\"" + UUID.randomUUID()
                                + "\",\"quantityPrescribed\":1,\"dosageInstructions\":\"...\"}",
                        authHeaders(List.of("PHARMACY_ORDER_VIEW"))),
                String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }
}
