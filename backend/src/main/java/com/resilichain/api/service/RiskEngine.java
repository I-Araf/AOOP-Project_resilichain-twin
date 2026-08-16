package com.resilichain.api.service;

import com.resilichain.api.domain.DisruptionSeverity;
import com.resilichain.api.domain.NetworkNode;
import com.resilichain.api.domain.Route;
import com.resilichain.api.domain.Shipment;
import com.resilichain.api.domain.ShipmentStatus;
import com.resilichain.api.domain.Warehouse;
import com.resilichain.api.repository.NetworkNodeRepository;
import com.resilichain.api.repository.RouteRepository;
import com.resilichain.api.repository.ShipmentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Deterministic, rule-based (no ML) impact analysis: given a disrupted node, breadth-first
 * traverses {@link Route} edges (bidirectionally, since a disrupted node blocks flow both
 * inbound and outbound through it) to find every reachable node, route and shipment, then
 * scores the overall risk from severity plus the size of that blast radius.
 */
@Service
public class RiskEngine {

    private static final int SEVERITY_WEIGHT_LOW = 10;
    private static final int SEVERITY_WEIGHT_MEDIUM = 25;
    private static final int SEVERITY_WEIGHT_HIGH = 45;
    private static final int SEVERITY_WEIGHT_CRITICAL = 65;
    private static final int MAX_SHIPMENT_CONTRIBUTION = 25;
    private static final int MAX_WAREHOUSE_CONTRIBUTION = 15;
    private static final int MAX_RISK_SCORE = 100;

    private final NetworkNodeRepository networkNodeRepository;
    private final RouteRepository routeRepository;
    private final ShipmentRepository shipmentRepository;

    public RiskEngine(NetworkNodeRepository networkNodeRepository, RouteRepository routeRepository,
                       ShipmentRepository shipmentRepository) {
        this.networkNodeRepository = networkNodeRepository;
        this.routeRepository = routeRepository;
        this.shipmentRepository = shipmentRepository;
    }

    public ImpactAssessment assess(NetworkNode disruptedNode, DisruptionSeverity severity) {
        List<Route> allRoutes = routeRepository.findAll();
        Map<Long, List<Route>> routesByNodeId = adjacency(allRoutes);

        Set<Long> visitedNodeIds = new LinkedHashSet<>();
        Set<Long> affectedRouteIds = new LinkedHashSet<>();
        Queue<Long> queue = new ArrayDeque<>();
        queue.add(disruptedNode.getId());

        Set<Long> queuedOrVisited = new LinkedHashSet<>();
        queuedOrVisited.add(disruptedNode.getId());

        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            for (Route route : routesByNodeId.getOrDefault(currentId, List.of())) {
                affectedRouteIds.add(route.getId());
                Long otherId = otherEndpoint(route, currentId);
                if (queuedOrVisited.add(otherId)) {
                    visitedNodeIds.add(otherId);
                    queue.add(otherId);
                }
            }
        }

        List<NetworkNode> affectedNodes = networkNodeRepository.findAllById(visitedNodeIds);
        List<Route> affectedRoutes = allRoutes.stream()
                .filter(route -> affectedRouteIds.contains(route.getId()))
                .toList();
        List<Shipment> affectedShipments = shipmentRepository.findAll().stream()
                .filter(shipment -> affectedRouteIds.contains(shipment.getRoute().getId()))
                .filter(shipment -> shipment.getStatus() != ShipmentStatus.DELIVERED
                        && shipment.getStatus() != ShipmentStatus.CANCELLED)
                .toList();
        List<Warehouse> affectedWarehouses = affectedNodes.stream()
                .filter(Warehouse.class::isInstance)
                .map(Warehouse.class::cast)
                .toList();

        int riskScore = computeRiskScore(severity, affectedShipments.size(), affectedWarehouses.size());

        return new ImpactAssessment(disruptedNode, affectedNodes, affectedRoutes, affectedShipments,
                affectedWarehouses, severity, riskScore);
    }

    private static Map<Long, List<Route>> adjacency(List<Route> routes) {
        Map<Long, List<Route>> byNodeId = new HashMap<>();
        for (Route route : routes) {
            byNodeId.computeIfAbsent(route.getOrigin().getId(), id -> new ArrayList<>()).add(route);
            byNodeId.computeIfAbsent(route.getDestination().getId(), id -> new ArrayList<>()).add(route);
        }
        return byNodeId;
    }

    private static Long otherEndpoint(Route route, Long fromNodeId) {
        return route.getOrigin().getId().equals(fromNodeId)
                ? route.getDestination().getId()
                : route.getOrigin().getId();
    }

    private static int computeRiskScore(DisruptionSeverity severity, int affectedShipmentCount, int affectedWarehouseCount) {
        int severityWeight = switch (severity) {
            case LOW -> SEVERITY_WEIGHT_LOW;
            case MEDIUM -> SEVERITY_WEIGHT_MEDIUM;
            case HIGH -> SEVERITY_WEIGHT_HIGH;
            case CRITICAL -> SEVERITY_WEIGHT_CRITICAL;
        };
        int shipmentContribution = Math.min(MAX_SHIPMENT_CONTRIBUTION, affectedShipmentCount * 2);
        int warehouseContribution = Math.min(MAX_WAREHOUSE_CONTRIBUTION, affectedWarehouseCount * 5);
        return Math.min(MAX_RISK_SCORE, severityWeight + shipmentContribution + warehouseContribution);
    }
}
