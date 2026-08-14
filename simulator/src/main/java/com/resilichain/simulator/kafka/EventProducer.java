package com.resilichain.simulator.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.resilichain.simulator.event.DisruptionEvent;
import com.resilichain.simulator.event.ShipmentEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public final class EventProducer implements AutoCloseable {

    public static final String SHIPMENT_EVENTS_TOPIC = "shipment-events";
    public static final String DISRUPTION_EVENTS_TOPIC = "disruption-events";

    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;

    public EventProducer(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        this.producer = new KafkaProducer<>(props);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void publishShipmentEvent(ShipmentEvent event) {
        send(SHIPMENT_EVENTS_TOPIC, String.valueOf(event.shipmentId()), event);
    }

    public void publishDisruptionEvent(DisruptionEvent event) {
        send(DISRUPTION_EVENTS_TOPIC, String.valueOf(event.nodeId()), event);
    }

    private void send(String topic, String key, Object payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            System.err.println("[Kafka] Failed to serialize event for topic " + topic + ": " + e.getMessage());
            return;
        }
        producer.send(new ProducerRecord<>(topic, key, json), (metadata, exception) -> {
            if (exception != null) {
                System.err.println("[Kafka] Failed to publish to " + topic + ": " + exception.getMessage());
            } else {
                System.out.println("[Kafka] -> " + topic + " key=" + key + " " + json);
            }
        });
    }

    @Override
    public void close() {
        producer.flush();
        producer.close();
    }
}
