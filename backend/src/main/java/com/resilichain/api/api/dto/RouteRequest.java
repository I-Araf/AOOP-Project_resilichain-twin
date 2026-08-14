package com.resilichain.api.api.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RouteRequest(
        @NotNull Long originId,
        @NotNull Long destinationId,
        @NotNull BigDecimal cost,
        int leadTimeHours,
        int capacity
) {
}
