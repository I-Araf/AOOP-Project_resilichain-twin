package com.resilichain.api.service;

import com.resilichain.api.domain.DisruptionSeverity;
import com.resilichain.api.domain.NetworkNode;
import com.resilichain.api.domain.Port;
import com.resilichain.api.kafka.dto.DisruptionEventPayload;
import com.resilichain.api.repository.NetworkNodeRepository;
import com.resilichain.api.websocket.dto.NodeStatusUpdateMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class NodeDisruptionService {

    private final NetworkNodeRepository networkNodeRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NodeDisruptionService(NetworkNodeRepository networkNodeRepository, SimpMessagingTemplate messagingTemplate) {
        this.networkNodeRepository = networkNodeRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void apply(DisruptionEventPayload payload) {
        NetworkNode node = networkNodeRepository.findById(payload.nodeId())
                .orElseThrow(() -> new NoSuchElementException("No network node with id " + payload.nodeId()));

        DisruptionSeverity severity = DisruptionSeverity.parse(payload.severity());
        node.setStatus(severity.toNodeStatus());
        if (node instanceof Port port) {
            port.setOperationalStatus(severity.toPortOperationalStatus());
        }
        networkNodeRepository.save(node);

        messagingTemplate.convertAndSend("/topic/network", NodeStatusUpdateMessage.from(node, payload.occurredAt()));
    }
}
