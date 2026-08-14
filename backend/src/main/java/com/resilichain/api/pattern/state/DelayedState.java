package com.resilichain.api.pattern.state;

import com.resilichain.api.domain.ShipmentStatus;

public final class DelayedState implements ShipmentState {

    public static final DelayedState INSTANCE = new DelayedState();

    private DelayedState() {
    }

    @Override
    public ShipmentStatus status() {
        return ShipmentStatus.DELAYED;
    }

    @Override
    public ShipmentState resumeTransit() {
        return InTransitState.INSTANCE;
    }

    @Override
    public ShipmentState reroute() {
        return ReroutedState.INSTANCE;
    }

    @Override
    public ShipmentState cancel() {
        return CancelledState.INSTANCE;
    }
}
