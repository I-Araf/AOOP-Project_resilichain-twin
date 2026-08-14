package com.resilichain.api.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resilichain.api.kafka.dto.DisruptionEventPayload;
import com.resilichain.api.kafka.dto.ShipmentEventPayload;
import com.resilichain.api.service.NodeDisruptionService;
import com.resilichain.api.service.ShipmentEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Reads the simulator's shipment-events/disruption-events topics (see simulator/), applies each
 * event to Postgres and broadcasts the result over STOMP. A malformed payload or an event that
 * doesn't fit the current DB state (unknown id, illegal state transition) is logged and dropped
 * rather than thrown: with the default error handler, an uncaught exception here would retry the
 * same record forever and stall the consumer on one bad message.
 */
@Component
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    private final ObjectMapper objectMapper;
    private final ShipmentEventService shipmentEventService;
    private final NodeDisruptionService nodeDisruptionService;

    public EventConsumer(ObjectMapper objectMapper, ShipmentEventService shipmentEventService,
                          NodeDisruptionService nodeDisruptionService) {
        this.objectMapper = objectMapper;
        this.shipmentEventService = shipmentEventService;
        this.nodeDisruptionService = nodeDisruptionService;
    }

    @KafkaListener(topics = "shipment-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onShipmentEvent(String message) {
        try {
            ShipmentEventPayload payload = objectMapper.readValue(message, ShipmentEventPayload.class);
            shipmentEventService.apply(payload);
        } catch (Exception e) {
            log.warn("Dropping unprocessable shipment event: {} ({})", message, e.getMessage());
        }
    }

    @KafkaListener(topics = "disruption-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onDisruptionEvent(String message) {
        try {
            DisruptionEventPayload payload = objectMapper.readValue(message, DisruptionEventPayload.class);
            nodeDisruptionService.apply(payload);
        } catch (Exception e) {
            log.warn("Dropping unprocessable disruption event: {} ({})", message, e.getMessage());
        }
    }
}
