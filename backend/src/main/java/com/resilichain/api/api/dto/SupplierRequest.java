package com.resilichain.api.api.dto;

import com.resilichain.api.domain.NodeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SupplierRequest(
        @NotBlank String name,
        double latitude,
        double longitude,
        @NotNull NodeStatus status,
        double reliabilityRating,
        int leadTimeCapabilityDays
) {
}
