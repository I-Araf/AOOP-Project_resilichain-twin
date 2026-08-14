package com.resilichain.api.pattern.state;

import com.resilichain.api.domain.ShipmentStatus;

/** State pattern for the shipment lifecycle: Planned -> InTransit -> Delayed -> Rerouted -> Delivered/Cancelled. */
public interface ShipmentState {

    ShipmentStatus status();

    default ShipmentState start() {
        throw invalidTransition("start");
    }

    default ShipmentState delay() {
        throw invalidTransition("delay");
    }

    default ShipmentState resumeTransit() {
        throw invalidTransition("resumeTransit");
    }

    default ShipmentState reroute() {
        throw invalidTransition("reroute");
    }

    default ShipmentState deliver() {
        throw invalidTransition("deliver");
    }

    default ShipmentState cancel() {
        throw invalidTransition("cancel");
    }

    private IllegalStateException invalidTransition(String action) {
        return new IllegalStateException(action + " is not a valid transition from " + status());
    }

    static ShipmentState of(ShipmentStatus status) {
        return switch (status) {
            case PLANNED -> PlannedState.INSTANCE;
            case IN_TRANSIT -> InTransitState.INSTANCE;
            case DELAYED -> DelayedState.INSTANCE;
            case REROUTED -> ReroutedState.INSTANCE;
            case DELIVERED -> DeliveredState.INSTANCE;
            case CANCELLED -> CancelledState.INSTANCE;
        };
    }
}
