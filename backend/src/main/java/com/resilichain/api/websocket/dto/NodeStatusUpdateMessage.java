package com.resilichain.api.websocket.dto;

import com.resilichain.api.domain.NetworkNode;
import com.resilichain.api.domain.NodeStatus;
import com.resilichain.api.domain.Port;
import com.resilichain.api.domain.PortOperationalStatus;

import java.time.Instant;

public record NodeStatusUpdateMessage(
        long nodeId,
        String nodeName,
        NodeStatus status,
        PortOperationalStatus operationalStatus,
        Instant occurredAt
) {
    public static NodeStatusUpdateMessage from(NetworkNode node, Instant occurredAt) {
        PortOperationalStatus operationalStatus = node instanceof Port port ? port.getOperationalStatus() : null;
        return new NodeStatusUpdateMessage(node.getId(), node.getName(), node.getStatus(), operationalStatus, occurredAt);
    }
}
