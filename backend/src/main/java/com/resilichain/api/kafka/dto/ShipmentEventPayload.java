package com.resilichain.api.kafka.dto;

import com.resilichain.api.domain.ShipmentStatus;

import java.time.Instant;

/** Mirrors the JSON shape published by the simulator (ShipmentEvent) onto the shipment-events topic. */
public record ShipmentEventPayload(
        long shipmentId,
        long routeId,
        long originId,
        long destinationId,
        ShipmentStatus fromStatus,
        ShipmentStatus toStatus,
        int quantity,
        Instant occurredAt
) {
}
