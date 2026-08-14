package com.resilichain.api.kafka.dto;

import java.time.Instant;

/** Mirrors the JSON shape published by the simulator (DisruptionEvent) onto the disruption-events topic. */
public record DisruptionEventPayload(
        long nodeId,
        String nodeName,
        String severity,
        int durationHours,
        String description,
        Instant occurredAt
) {
}
