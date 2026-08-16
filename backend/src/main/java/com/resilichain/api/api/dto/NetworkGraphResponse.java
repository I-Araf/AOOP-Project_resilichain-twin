package com.resilichain.api.api.dto;

import com.resilichain.api.domain.NetworkNode;
import com.resilichain.api.domain.Route;

import java.util.List;

public record NetworkGraphResponse(
        List<NetworkNodeSummaryResponse> nodes,
        List<RouteResponse> routes
) {
    public static NetworkGraphResponse from(List<NetworkNode> nodes, List<Route> routes) {
        return new NetworkGraphResponse(
                nodes.stream().map(NetworkNodeSummaryResponse::from).toList(),
                routes.stream().map(RouteResponse::from).toList());
    }
}
