package com.resilichain.api.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShipmentTest {

    private final Warehouse warehouse = new Warehouse("WH", 23.7, 90.3, NodeStatus.OPERATIONAL, 1000, 200);
    private final Port port = new Port("Port", 22.3, 91.8, NodeStatus.OPERATIONAL, 5000, PortOperationalStatus.OPEN);
    private final Route route = new Route(warehouse, port, new BigDecimal("100.00"), 10, 500);

    @Test
    void constructingWithValidValuesDefaultsToPlannedStatus() {
        Instant eta = Instant.now();
        Shipment shipment = new Shipment(route, warehouse, port, 50, eta);

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PLANNED);
        assertThat(shipment.getQuantity()).isEqualTo(50);
        assertThat(shipment.getRoute()).isEqualTo(route);
        assertThat(shipment.getEta()).isEqualTo(eta);
    }

    @Test
    void nullRouteThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Shipment(null, warehouse, port, 1, Instant.now()));
    }

    @Test
    void nullOriginOrDestinationThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Shipment(route, null, port, 1, Instant.now()));
        assertThrows(IllegalArgumentException.class,
                () -> new Shipment(route, warehouse, null, 1, Instant.now()));
    }

    @Test
    void sameOriginAndDestinationThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Shipment(route, warehouse, warehouse, 1, Instant.now()));
    }

    @Test
    void zeroOrNegativeQuantityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Shipment(route, warehouse, port, 0, Instant.now()));
        assertThrows(IllegalArgumentException.class,
                () -> new Shipment(route, warehouse, port, -5, Instant.now()));
    }

    @Test
    void fullLifecycleTransitionsThroughEachState() {
        Shipment shipment = new Shipment(route, warehouse, port, 1, Instant.now());

        shipment.startTransit();
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);

        shipment.markDelayed();
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELAYED);

        shipment.reroute();
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.REROUTED);

        shipment.markDelivered();
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
    }

    @Test
    void delayedShipmentCanResumeTransitWithoutRerouting() {
        Shipment shipment = new Shipment(route, warehouse, port, 1, Instant.now());
        shipment.startTransit();
        shipment.markDelayed();

        shipment.resumeTransit();

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    }

    @Test
    void cancelIsAllowedFromPlannedInTransitDelayedAndRerouted() {
        Shipment planned = new Shipment(route, warehouse, port, 1, Instant.now());
        planned.cancel();
        assertThat(planned.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);

        Shipment inTransit = new Shipment(route, warehouse, port, 1, Instant.now());
        inTransit.startTransit();
        inTransit.cancel();
        assertThat(inTransit.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);

        Shipment delayed = new Shipment(route, warehouse, port, 1, Instant.now());
        delayed.startTransit();
        delayed.markDelayed();
        delayed.cancel();
        assertThat(delayed.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);

        Shipment rerouted = new Shipment(route, warehouse, port, 1, Instant.now());
        rerouted.startTransit();
        rerouted.markDelayed();
        rerouted.reroute();
        rerouted.cancel();
        assertThat(rerouted.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
    }

    @Test
    void skippingStatesThrowsIllegalState() {
        Shipment shipment = new Shipment(route, warehouse, port, 1, Instant.now());
        assertThrows(IllegalStateException.class, shipment::markDelivered);
        assertThrows(IllegalStateException.class, shipment::markDelayed);
        assertThrows(IllegalStateException.class, shipment::reroute);
    }

    @Test
    void terminalStatesRejectFurtherTransitions() {
        Shipment delivered = new Shipment(route, warehouse, port, 1, Instant.now());
        delivered.startTransit();
        delivered.markDelivered();
        assertThrows(IllegalStateException.class, delivered::startTransit);
        assertThrows(IllegalStateException.class, delivered::cancel);

        Shipment cancelled = new Shipment(route, warehouse, port, 1, Instant.now());
        cancelled.cancel();
        assertThrows(IllegalStateException.class, cancelled::startTransit);
        assertThrows(IllegalStateException.class, cancelled::markDelivered);
    }

    @Test
    void advanceToInTransitPicksStartOrResumeBasedOnCurrentStatus() {
        Shipment fromPlanned = new Shipment(route, warehouse, port, 1, Instant.now());
        fromPlanned.advanceTo(ShipmentStatus.IN_TRANSIT);
        assertThat(fromPlanned.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);

        Shipment fromDelayed = new Shipment(route, warehouse, port, 1, Instant.now());
        fromDelayed.startTransit();
        fromDelayed.markDelayed();
        fromDelayed.advanceTo(ShipmentStatus.IN_TRANSIT);
        assertThat(fromDelayed.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    }

    @Test
    void advanceToDispatchesToTheMatchingNamedTransition() {
        Shipment shipment = new Shipment(route, warehouse, port, 1, Instant.now());

        shipment.advanceTo(ShipmentStatus.IN_TRANSIT);
        shipment.advanceTo(ShipmentStatus.DELAYED);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELAYED);

        shipment.advanceTo(ShipmentStatus.REROUTED);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.REROUTED);

        shipment.advanceTo(ShipmentStatus.DELIVERED);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
    }

    @Test
    void advanceToCancelledCancelsFromAnyNonTerminalStatus() {
        Shipment shipment = new Shipment(route, warehouse, port, 1, Instant.now());
        shipment.advanceTo(ShipmentStatus.CANCELLED);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
    }

    @Test
    void advanceToPlannedIsNeverValid() {
        Shipment shipment = new Shipment(route, warehouse, port, 1, Instant.now());
        assertThrows(IllegalStateException.class, () -> shipment.advanceTo(ShipmentStatus.PLANNED));
    }

    @Test
    void advanceToAnUnreachableTargetThrows() {
        Shipment shipment = new Shipment(route, warehouse, port, 1, Instant.now());
        assertThrows(IllegalStateException.class, () -> shipment.advanceTo(ShipmentStatus.DELIVERED));
    }
}
