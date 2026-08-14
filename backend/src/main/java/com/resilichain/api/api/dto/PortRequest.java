package com.resilichain.api.api.dto;

import com.resilichain.api.domain.NodeStatus;
import com.resilichain.api.domain.PortOperationalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PortRequest(
        @NotBlank String name,
        double latitude,
        double longitude,
        @NotNull NodeStatus status,
        int throughputCapacity,
        @NotNull PortOperationalStatus operationalStatus
) {
}
