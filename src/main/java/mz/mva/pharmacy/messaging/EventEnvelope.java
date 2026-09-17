package mz.mva.pharmacy.messaging;

import java.time.Instant;
import java.util.UUID;

/**
 * AGENTS.md §44's Kafka event envelope. Every producer service in this
 * platform defines its own copy of this shape (no shared Kafka/event library
 * — same "no shared entities across repositories" rule as every other
 * cross-service concept).
 */
public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant timestamp,
        UUID correlationId,
        UUID causationId,
        String producer,
        T payload) {

    public static <T> EventEnvelope<T> of(String eventType, UUID correlationId, String producer, T payload) {
        return new EventEnvelope<>(UUID.randomUUID(), eventType, 1, Instant.now(), correlationId, null, producer, payload);
    }
}
