package com.resilichain.api.api.dto;

import com.resilichain.api.domain.NodeStatus;
import com.resilichain.api.domain.Supplier;

import java.time.Instant;

public record SupplierResponse(
        Long id,
        String name,
        double latitude,
        double longitude,
        NodeStatus status,
        Instant createdAt,
        double reliabilityRating,
        int leadTimeCapabilityDays
) {
    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(supplier.getId(), supplier.getName(), supplier.getLatitude(),
                supplier.getLongitude(), supplier.getStatus(), supplier.getCreatedAt(),
                supplier.getReliabilityRating(), supplier.getLeadTimeCapabilityDays());
    }
}
