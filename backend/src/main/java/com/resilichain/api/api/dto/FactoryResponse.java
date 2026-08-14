package com.resilichain.api.api.dto;

import com.resilichain.api.domain.Factory;
import com.resilichain.api.domain.NodeStatus;

import java.time.Instant;

public record FactoryResponse(
        Long id,
        String name,
        double latitude,
        double longitude,
        NodeStatus status,
        Instant createdAt,
        int productionCapacity,
        int currentOutput
) {
    public static FactoryResponse from(Factory factory) {
        return new FactoryResponse(factory.getId(), factory.getName(), factory.getLatitude(),
                factory.getLongitude(), factory.getStatus(), factory.getCreatedAt(),
                factory.getProductionCapacity(), factory.getCurrentOutput());
    }
}
