package com.resilichain.api.websocket.dto;

import com.resilichain.api.domain.Shipment;
import com.resilichain.api.domain.ShipmentStatus;

import java.time.Instant;

public record ShipmentUpdateMessage(
        long shipmentId,
        ShipmentStatus status,
        Instant occurredAt
) {
    public static ShipmentUpdateMessage from(Shipment shipment, Instant occurredAt) {
        return new ShipmentUpdateMessage(shipment.getId(), shipment.getStatus(), occurredAt);
    }
}
