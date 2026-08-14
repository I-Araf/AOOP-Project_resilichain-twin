package com.resilichain.simulator.event;

import java.time.Instant;

public record DisruptionEvent(
        long nodeId,
        String nodeName,
        String severity,
        int durationHours,
        String description,
        Instant occurredAt
) {
}
