package com.resilichain.api.api.dto;

import com.resilichain.api.domain.Disruption;
import com.resilichain.api.domain.DisruptionSeverity;
import com.resilichain.api.service.ImpactAssessment;

import java.time.Instant;

public record DisruptionImpactResponse(
        Long disruptionId,
        Long targetNodeId,
        String targetNodeName,
        DisruptionSeverity severity,
        int durationHours,
        Instant startTime,
        int affectedNodeCount,
        int affectedRouteCount,
        int affectedShipmentCount,
        int affectedWarehouseCount,
        int riskScore
) {
    public static DisruptionImpactResponse from(Disruption disruption, ImpactAssessment impact) {
        return new DisruptionImpactResponse(
                disruption.getId(),
                disruption.getTargetNode().getId(),
                disruption.getTargetNode().getName(),
                disruption.getSeverity(),
                disruption.getDurationHours(),
                disruption.getStartTime(),
                impact.affectedNodes().size(),
                impact.affectedRoutes().size(),
                impact.affectedShipments().size(),
                impact.affectedWarehouses().size(),
                impact.riskScore());
    }
}
