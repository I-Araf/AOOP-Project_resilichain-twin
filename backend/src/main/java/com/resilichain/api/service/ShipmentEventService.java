package com.resilichain.api.service;

import com.resilichain.api.domain.Shipment;
import com.resilichain.api.kafka.dto.ShipmentEventPayload;
import com.resilichain.api.repository.ShipmentRepository;
import com.resilichain.api.websocket.dto.ShipmentUpdateMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class ShipmentEventService {

    private final ShipmentRepository shipmentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ShipmentEventService(ShipmentRepository shipmentRepository, SimpMessagingTemplate messagingTemplate) {
        this.shipmentRepository = shipmentRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void apply(ShipmentEventPayload payload) {
        Shipment shipment = shipmentRepository.findById(payload.shipmentId())
                .orElseThrow(() -> new NoSuchElementException("No shipment with id " + payload.shipmentId()));

        shipment.advanceTo(payload.toStatus());
        shipmentRepository.save(shipment);

        messagingTemplate.convertAndSend("/topic/shipments", ShipmentUpdateMessage.from(shipment, payload.occurredAt()));
    }
}
