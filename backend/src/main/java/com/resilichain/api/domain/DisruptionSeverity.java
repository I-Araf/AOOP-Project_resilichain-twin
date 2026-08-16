package com.resilichain.api.domain;

import java.util.Locale;

/** Classifies an incoming disruption signal's impact on the node it targets. */
public enum DisruptionSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static DisruptionSeverity parse(String raw) {
        if (raw == null) {
            return HIGH;
        }
        try {
            return DisruptionSeverity.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return HIGH;
        }
    }

    public NodeStatus toNodeStatus() {
        return switch (this) {
            case LOW, MEDIUM -> NodeStatus.DEGRADED;
            case HIGH, CRITICAL -> NodeStatus.DISRUPTED;
        };
    }

    public PortOperationalStatus toPortOperationalStatus() {
        return switch (this) {
            case LOW, MEDIUM -> PortOperationalStatus.CONGESTED;
            case HIGH, CRITICAL -> PortOperationalStatus.CLOSED;
        };
    }

    /** Mutates the given node's status (and its port operational status, if applicable) to reflect this severity. */
    public void applyTo(NetworkNode node) {
        node.setStatus(this.toNodeStatus());
        if (node instanceof Port port) {
            port.setOperationalStatus(this.toPortOperationalStatus());
        }
    }
}
