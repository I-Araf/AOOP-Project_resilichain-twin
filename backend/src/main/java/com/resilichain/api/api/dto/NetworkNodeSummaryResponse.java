package com.resilichain.api.api.dto;

import com.resilichain.api.domain.Factory;
import com.resilichain.api.domain.NetworkNode;
import com.resilichain.api.domain.NodeStatus;
import com.resilichain.api.domain.Port;
import com.resilichain.api.domain.Supplier;
import com.resilichain.api.domain.Warehouse;

import java.time.Instant;

public record NetworkNodeSummaryResponse(
        Long id,
        String nodeType,
        String name,
        double latitude,
        double longitude,
        NodeStatus status,
        Instant createdAt
) {
    public static NetworkNodeSummaryResponse from(NetworkNode node) {
        return new NetworkNodeSummaryResponse(node.getId(), nodeType(node), node.getName(),
                node.getLatitude(), node.getLongitude(), node.getStatus(), node.getCreatedAt());
    }

    private static String nodeType(NetworkNode node) {
        return switch (node) {
            case Supplier s -> "SUPPLIER";
            case Factory f -> "FACTORY";
            case Warehouse w -> "WAREHOUSE";
            case Port p -> "PORT";
            default -> throw new IllegalStateException("Unknown NetworkNode subtype: " + node.getClass());
        };
    }
}
