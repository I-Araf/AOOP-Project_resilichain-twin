package com.resilichain.api.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DisruptionTest {

    private final Port port = new Port("Port", 22.3, 91.8, NodeStatus.OPERATIONAL, 5000, PortOperationalStatus.OPEN);
    private final Warehouse warehouse = new Warehouse("WH", 23.7, 90.3, NodeStatus.OPERATIONAL, 1000, 200);
    private final Route route = new Route(warehouse, port, new BigDecimal("150.00"), 12, 500);

    @Test
    void forNodeConstructsWithNodeTargetOnly() {
        Instant startTime = Instant.now();
        Disruption disruption = Disruption.forNode(port, DisruptionSeverity.HIGH, 36, startTime);

        assertThat(disruption.getTargetNode()).isEqualTo(port);
        assertThat(disruption.getTargetRoute()).isNull();
        assertThat(disruption.getSeverity()).isEqualTo(DisruptionSeverity.HIGH);
        assertThat(disruption.getDurationHours()).isEqualTo(36);
        assertThat(disruption.getStartTime()).isEqualTo(startTime);
    }

    @Test
    void forRouteConstructsWithRouteTargetOnly() {
        Disruption disruption = Disruption.forRoute(route, DisruptionSeverity.MEDIUM, 6, Instant.now());

        assertThat(disruption.getTargetNode()).isNull();
        assertThat(disruption.getTargetRoute()).isEqualTo(route);
    }

    @Test
    void bothTargetsNullThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Disruption(null, null, DisruptionSeverity.LOW, 1, Instant.now()));
    }

    @Test
    void bothTargetsSetThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Disruption(port, route, DisruptionSeverity.LOW, 1, Instant.now()));
    }

    @Test
    void nonPositiveDurationThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> Disruption.forNode(port, DisruptionSeverity.LOW, 0, Instant.now()));
        assertThrows(IllegalArgumentException.class,
                () -> Disruption.forNode(port, DisruptionSeverity.LOW, -1, Instant.now()));
    }

    @Test
    void nullSeverityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> Disruption.forNode(port, null, 1, Instant.now()));
    }

    @Test
    void nullStartTimeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> Disruption.forNode(port, DisruptionSeverity.LOW, 1, null));
    }
}
