package com.resilichain.api.api;

import com.resilichain.api.api.dto.DisruptionImpactResponse;
import com.resilichain.api.api.dto.DisruptionTriggerRequest;
import com.resilichain.api.service.DisruptionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/disruptions")
public class DisruptionController {

    private final DisruptionService disruptionService;

    public DisruptionController(DisruptionService disruptionService) {
        this.disruptionService = disruptionService;
    }

    @PostMapping
    public DisruptionImpactResponse triggerDisruption(@Valid @RequestBody DisruptionTriggerRequest request) {
        DisruptionService.TriggerResult result = disruptionService.trigger(
                request.targetNodeId(), request.severity(), request.durationHours());
        return DisruptionImpactResponse.from(result.disruption(), result.impact());
    }
}
