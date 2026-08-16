package com.resilichain.api.service;

import com.resilichain.api.domain.Disruption;
import com.resilichain.api.domain.DisruptionSeverity;
import com.resilichain.api.domain.NetworkNode;
import com.resilichain.api.repository.DisruptionRepository;
import com.resilichain.api.repository.NetworkNodeRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
public class DisruptionService {

    private final NetworkNodeRepository networkNodeRepository;
    private final DisruptionRepository disruptionRepository;
    private final RiskEngine riskEngine;

    public DisruptionService(NetworkNodeRepository networkNodeRepository, DisruptionRepository disruptionRepository,
                              RiskEngine riskEngine) {
        this.networkNodeRepository = networkNodeRepository;
        this.disruptionRepository = disruptionRepository;
        this.riskEngine = riskEngine;
    }

    public TriggerResult trigger(Long targetNodeId, DisruptionSeverity severity, int durationHours) {
        NetworkNode node = networkNodeRepository.findById(targetNodeId)
                .orElseThrow(() -> new NoSuchElementException("No network node with id " + targetNodeId));

        ImpactAssessment impact = riskEngine.assess(node, severity);

        severity.applyTo(node);
        networkNodeRepository.save(node);

        Disruption disruption = disruptionRepository.save(
                Disruption.forNode(node, severity, durationHours, Instant.now()));

        return new TriggerResult(disruption, impact);
    }

    public record TriggerResult(Disruption disruption, ImpactAssessment impact) {
    }
}
