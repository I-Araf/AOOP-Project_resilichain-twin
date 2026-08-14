package com.resilichain.api.api.dto;

import com.resilichain.api.domain.NodeStatus;
import com.resilichain.api.domain.Warehouse;

import java.time.Instant;

public record WarehouseResponse(
        Long id,
        String name,
        double latitude,
        double longitude,
        NodeStatus status,
        Instant createdAt,
        int capacity,
        int currentStock
) {
    public static WarehouseResponse from(Warehouse warehouse) {
        return new WarehouseResponse(warehouse.getId(), warehouse.getName(), warehouse.getLatitude(),
                warehouse.getLongitude(), warehouse.getStatus(), warehouse.getCreatedAt(),
                warehouse.getCapacity(), warehouse.getCurrentStock());
    }
}
