package com.resilichain.api.pattern.state;

import com.resilichain.api.domain.ShipmentStatus;

public final class PlannedState implements ShipmentState {

    public static final PlannedState INSTANCE = new PlannedState();

    private PlannedState() {
    }

    @Override
    public ShipmentStatus status() {
        return ShipmentStatus.PLANNED;
    }

    @Override
    public ShipmentState start() {
        return InTransitState.INSTANCE;
    }

    @Override
    public ShipmentState cancel() {
        return CancelledState.INSTANCE;
    }
}
