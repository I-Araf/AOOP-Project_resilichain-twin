package com.resilichain.api.api.dto;

import com.resilichain.api.domain.Route;

import java.math.BigDecimal;

public record RouteResponse(
        Long id,
        Long originId,
        String originName,
        Long destinationId,
        String destinationName,
        BigDecimal cost,
        int leadTimeHours,
        int capacity
) {
    public static RouteResponse from(Route route) {
        return new RouteResponse(route.getId(), route.getOrigin().getId(), route.getOrigin().getName(),
                route.getDestination().getId(), route.getDestination().getName(),
                route.getCost(), route.getLeadTimeHours(), route.getCapacity());
    }
}
