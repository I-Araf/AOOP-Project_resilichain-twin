package com.resilichain.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DisruptionSeverityTest {

    @Test
    void parsesKnownSeveritiesCaseInsensitively() {
        assertThat(DisruptionSeverity.parse("low")).isEqualTo(DisruptionSeverity.LOW);
        assertThat(DisruptionSeverity.parse("Medium")).isEqualTo(DisruptionSeverity.MEDIUM);
        assertThat(DisruptionSeverity.parse("HIGH")).isEqualTo(DisruptionSeverity.HIGH);
        assertThat(DisruptionSeverity.parse("critical")).isEqualTo(DisruptionSeverity.CRITICAL);
    }

    @Test
    void unknownOrNullSeverityFallsBackToHigh() {
        assertThat(DisruptionSeverity.parse("catastrophic")).isEqualTo(DisruptionSeverity.HIGH);
        assertThat(DisruptionSeverity.parse(null)).isEqualTo(DisruptionSeverity.HIGH);
        assertThat(DisruptionSeverity.parse("")).isEqualTo(DisruptionSeverity.HIGH);
    }

    @Test
    void lowAndMediumDegradeTheNodeAndCongestPorts() {
        assertThat(DisruptionSeverity.LOW.toNodeStatus()).isEqualTo(NodeStatus.DEGRADED);
        assertThat(DisruptionSeverity.MEDIUM.toNodeStatus()).isEqualTo(NodeStatus.DEGRADED);
        assertThat(DisruptionSeverity.LOW.toPortOperationalStatus()).isEqualTo(PortOperationalStatus.CONGESTED);
        assertThat(DisruptionSeverity.MEDIUM.toPortOperationalStatus()).isEqualTo(PortOperationalStatus.CONGESTED);
    }

    @Test
    void highAndCriticalDisruptTheNodeAndClosePorts() {
        assertThat(DisruptionSeverity.HIGH.toNodeStatus()).isEqualTo(NodeStatus.DISRUPTED);
        assertThat(DisruptionSeverity.CRITICAL.toNodeStatus()).isEqualTo(NodeStatus.DISRUPTED);
        assertThat(DisruptionSeverity.HIGH.toPortOperationalStatus()).isEqualTo(PortOperationalStatus.CLOSED);
        assertThat(DisruptionSeverity.CRITICAL.toPortOperationalStatus()).isEqualTo(PortOperationalStatus.CLOSED);
    }
}
