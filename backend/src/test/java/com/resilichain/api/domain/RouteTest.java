package com.resilichain.api.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RouteTest {

    private final Warehouse warehouse = new Warehouse("WH", 23.7, 90.3, NodeStatus.OPERATIONAL, 1000, 200);
    private final Port port = new Port("Port", 22.3, 91.8, NodeStatus.OPERATIONAL, 5000, PortOperationalStatus.OPEN);

    @Test
    void constructingWithValidValuesSucceeds() {
        Route route = new Route(warehouse, port, new BigDecimal("150.00"), 12, 500);

        assertThat(route.getOrigin()).isEqualTo(warehouse);
        assertThat(route.getDestination()).isEqualTo(port);
        assertThat(route.getCost()).isEqualByComparingTo("150.00");
        assertThat(route.getLeadTimeHours()).isEqualTo(12);
        assertThat(route.getCapacity()).isEqualTo(500);
    }

    @Test
    void nullOriginThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Route(null, port, BigDecimal.ZERO, 1, 1));
    }

    @Test
    void nullDestinationThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Route(warehouse, null, BigDecimal.ZERO, 1, 1));
    }

    @Test
    void sameOriginAndDestinationThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Route(warehouse, warehouse, BigDecimal.ZERO, 1, 1));
    }

    @Test
    void negativeCostThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Route(warehouse, port, new BigDecimal("-0.01"), 1, 1));
    }

    @Test
    void negativeLeadTimeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Route(warehouse, port, BigDecimal.ZERO, -1, 1));
    }

    @Test
    void zeroOrNegativeCapacityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Route(warehouse, port, BigDecimal.ZERO, 1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new Route(warehouse, port, BigDecimal.ZERO, 1, -1));
    }

    @Test
    void setDestinationToSameAsOriginThrows() {
        Route route = new Route(warehouse, port, BigDecimal.ZERO, 1, 1);
        assertThrows(IllegalArgumentException.class, () -> route.setDestination(warehouse));
    }
}
