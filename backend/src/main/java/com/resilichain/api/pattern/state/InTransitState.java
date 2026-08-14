package com.resilichain.api.pattern.state;

import com.resilichain.api.domain.ShipmentStatus;

public final class InTransitState implements ShipmentState {

    public static final InTransitState INSTANCE = new InTransitState();

    private InTransitState() {
    }

    @Override
    public ShipmentStatus status() {
        return ShipmentStatus.IN_TRANSIT;
    }

    @Override
    public ShipmentState delay() {
        return DelayedState.INSTANCE;
    }

    @Override
    public ShipmentState deliver() {
        return DeliveredState.INSTANCE;
    }

    @Override
    public ShipmentState cancel() {
        return CancelledState.INSTANCE;
    }
}
