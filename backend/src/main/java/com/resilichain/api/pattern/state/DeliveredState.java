package com.resilichain.api.pattern.state;

import com.resilichain.api.domain.ShipmentStatus;

/** Terminal state: a delivered shipment accepts no further transitions. */
public final class DeliveredState implements ShipmentState {

    public static final DeliveredState INSTANCE = new DeliveredState();

    private DeliveredState() {
    }

    @Override
    public ShipmentStatus status() {
        return ShipmentStatus.DELIVERED;
    }
}
