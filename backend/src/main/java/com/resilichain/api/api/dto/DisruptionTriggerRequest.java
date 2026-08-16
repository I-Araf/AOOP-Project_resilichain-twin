package com.resilichain.api.api.dto;

import com.resilichain.api.domain.DisruptionSeverity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DisruptionTriggerRequest(
        @NotNull Long targetNodeId,
        @NotNull DisruptionSeverity severity,
        @Positive int durationHours
) {
}
