package com.resilichain.api.api.dto;

import com.resilichain.api.domain.NodeStatus;
import com.resilichain.api.domain.Port;
import com.resilichain.api.domain.PortOperationalStatus;

import java.time.Instant;

public record PortResponse(
        Long id,
        String name,
        double latitude,
        double longitude,
        NodeStatus status,
        Instant createdAt,
        int throughputCapacity,
        PortOperationalStatus operationalStatus
) {
    public static PortResponse from(Port port) {
        return new PortResponse(port.getId(), port.getName(), port.getLatitude(),
                port.getLongitude(), port.getStatus(), port.getCreatedAt(),
                port.getThroughputCapacity(), port.getOperationalStatus());
    }
}
