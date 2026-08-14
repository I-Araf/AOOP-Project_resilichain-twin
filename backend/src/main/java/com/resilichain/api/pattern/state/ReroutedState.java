package com.resilichain.api.pattern.state;

import com.resilichain.api.domain.ShipmentStatus;

public final class ReroutedState implements ShipmentState {

    public static final ReroutedState INSTANCE = new ReroutedState();

    private ReroutedState() {
    }

    @Override
    public ShipmentStatus status() {
        return ShipmentStatus.REROUTED;
    }

    @Override
    public ShipmentState resumeTransit() {
        return InTransitState.INSTANCE;
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
