package com.resilichain.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortTest {

    @Test
    void constructingWithValidValuesSucceeds() {
        Port port = new Port("Chattogram Port", 22.3, 91.8, NodeStatus.OPERATIONAL, 20000, PortOperationalStatus.OPEN);

        assertThat(port.getThroughputCapacity()).isEqualTo(20000);
        assertThat(port.getOperationalStatus()).isEqualTo(PortOperationalStatus.OPEN);
    }

    @Test
    void zeroOrNegativeThroughputCapacityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Port("A", 0, 0, NodeStatus.OPERATIONAL, 0, PortOperationalStatus.OPEN));
        assertThrows(IllegalArgumentException.class,
                () -> new Port("A", 0, 0, NodeStatus.OPERATIONAL, -1, PortOperationalStatus.OPEN));
    }

    @Test
    void nullOperationalStatusThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Port("A", 0, 0, NodeStatus.OPERATIONAL, 100, null));
    }

    @Test
    void operationalStatusAcceptsAllEnumValues() {
        for (PortOperationalStatus status : PortOperationalStatus.values()) {
            Port port = new Port("A", 0, 0, NodeStatus.OPERATIONAL, 100, status);
            assertThat(port.getOperationalStatus()).isEqualTo(status);
        }
    }
}
