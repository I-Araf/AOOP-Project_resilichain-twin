package com.resilichain.simulator.event;

import com.resilichain.simulator.scenario.SimStatus;

import java.time.Instant;

public record ShipmentEvent(
        long shipmentId,
        long routeId,
        long originId,
        long destinationId,
        SimStatus fromStatus,
        SimStatus toStatus,
        int quantity,
        Instant occurredAt
) {
}
