package mz.mva.pharmacy.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    /**
     * Eureka-discovery-backed RestTemplate for calling hospital-clinical-service
     * directly (bypassing the gateway) — same pattern as Laboratory (Phase 4a)
     * and Radiology (Phase 4b).
     */
    @Bean
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
