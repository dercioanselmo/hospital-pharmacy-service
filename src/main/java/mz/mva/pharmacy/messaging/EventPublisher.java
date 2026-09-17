package mz.mva.pharmacy.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes to this service's own Kafka topic. A publish failure is logged,
 * not thrown — a notification not going out must never roll back or block
 * the dispensing transaction that triggered it (AGENTS.md §45: no
 * distributed transactions between services).
 */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public EventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${mva.kafka.topics.pharmacy}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void publish(String eventType, Object payload) {
        EventEnvelope<Object> envelope = EventEnvelope.of(eventType, UUID.randomUUID(), "pharmacy-service", payload);
        try {
            kafkaTemplate.send(topic, envelope.eventId().toString(), objectMapper.writeValueAsString(envelope));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event {} for publishing", eventType, e);
        }
    }
}
