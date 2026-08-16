package com.resilichain.api.service;

import com.resilichain.api.domain.DisruptionSeverity;
import com.resilichain.api.domain.NetworkNode;
import com.resilichain.api.domain.Route;
import com.resilichain.api.domain.Shipment;
import com.resilichain.api.domain.Warehouse;

import java.util.List;

/** Result of {@link RiskEngine#assess}: everything reachable from a disrupted node, plus a deterministic risk score. */
public record ImpactAssessment(
        NetworkNode disruptedNode,
        List<NetworkNode> affectedNodes,
        List<Route> affectedRoutes,
        List<Shipment> affectedShipments,
        List<Warehouse> affectedWarehouses,
        DisruptionSeverity severity,
        int riskScore
) {
}
