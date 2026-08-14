package com.resilichain.api.pattern.state;

import com.resilichain.api.domain.ShipmentStatus;

/** Terminal state: a cancelled shipment accepts no further transitions. */
public final class CancelledState implements ShipmentState {

    public static final CancelledState INSTANCE = new CancelledState();

    private CancelledState() {
    }

    @Override
    public ShipmentStatus status() {
        return ShipmentStatus.CANCELLED;
    }
}
