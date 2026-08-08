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
    void setStatusRejectsNull() {
        Shipment shipment = new Shipment(route, warehouse, port, 1, Instant.now());
        assertThrows(IllegalArgumentException.class, () -> shipment.setStatus(null));
    }

    @Test
    void setStatusAllowsLifecycleTransition() {
        Shipment shipment = new Shipment(route, warehouse, port, 1, Instant.now());
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    }
}
