package com.resilichain.api.kafka;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resilichain.api.domain.NodeStatus;
import com.resilichain.api.domain.Port;
import com.resilichain.api.domain.PortOperationalStatus;
import com.resilichain.api.domain.Route;
import com.resilichain.api.domain.Shipment;
import com.resilichain.api.domain.ShipmentStatus;
import com.resilichain.api.domain.Warehouse;
import com.resilichain.api.repository.PortRepository;
import com.resilichain.api.repository.RouteRepository;
import com.resilichain.api.repository.ShipmentRepository;
import com.resilichain.api.repository.WarehouseRepository;
import com.resilichain.api.websocket.dto.NodeStatusUpdateMessage;
import com.resilichain.api.websocket.dto.ShipmentUpdateMessage;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"shipment-events", "disruption-events"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.listener.auto-startup=true"
})
class EventConsumerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private PortRepository portRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    private KafkaProducer<String, String> producer;
    private Shipment shipment;
    private Long portId;

    @BeforeEach
    void seedFixturesAndProducer() {
        Warehouse warehouse = warehouseRepository.save(
                new Warehouse("Central WH", 23.7, 90.3, NodeStatus.OPERATIONAL, 1000, 200));
        Port seededPort = portRepository.save(
                new Port("Port of Chattogram", 22.3, 91.8, NodeStatus.OPERATIONAL, 5000, PortOperationalStatus.OPEN));
        Route route = routeRepository.save(new Route(warehouse, seededPort, new BigDecimal("100.00"), 10, 500));
        shipment = shipmentRepository.save(new Shipment(route, warehouse, seededPort, 50, Instant.now()));
        portId = seededPort.getId();

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(props);
    }

    @AfterEach
    void closeProducer() {
        producer.close();
    }

    @Test
    void shipmentEventAdvancesStatusInPostgresAndBroadcastsOverStomp() throws Exception {
        StompSession session = connectStomp();
        BlockingQueue<ShipmentUpdateMessage> received = new LinkedBlockingQueue<>();
        session.subscribe("/topic/shipments", frameHandlerFor(ShipmentUpdateMessage.class, received));

        String json = """
                {"shipmentId":%d,"routeId":%d,"originId":%d,"destinationId":%d,\
                "fromStatus":"PLANNED","toStatus":"IN_TRANSIT","quantity":50,\
                "occurredAt":"%s"}\
                """.formatted(shipment.getId(), shipment.getRoute().getId(), shipment.getOrigin().getId(),
                shipment.getDestination().getId(), Instant.now());

        producer.send(new ProducerRecord<>("shipment-events", String.valueOf(shipment.getId()), json)).get(5, TimeUnit.SECONDS);

        ShipmentUpdateMessage message = received.poll(10, TimeUnit.SECONDS);
        assertThat(message).isNotNull();
        assertThat(message.shipmentId()).isEqualTo(shipment.getId());
        assertThat(message.status()).isEqualTo(ShipmentStatus.IN_TRANSIT);

        Shipment reloaded = shipmentRepository.findById(shipment.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    }

    @Test
    void disruptionEventUpdatesPortInPostgresAndBroadcastsOverStomp() throws Exception {
        StompSession session = connectStomp();
        BlockingQueue<NodeStatusUpdateMessage> received = new LinkedBlockingQueue<>();
        session.subscribe("/topic/network", frameHandlerFor(NodeStatusUpdateMessage.class, received));

        String json = """
                {"nodeId":%d,"nodeName":"Port of Chattogram","severity":"CRITICAL",\
                "durationHours":36,"description":"test disruption","occurredAt":"%s"}\
                """.formatted(portId, Instant.now());

        producer.send(new ProducerRecord<>("disruption-events", String.valueOf(portId), json)).get(5, TimeUnit.SECONDS);

        NodeStatusUpdateMessage message = received.poll(10, TimeUnit.SECONDS);
        assertThat(message).isNotNull();
        assertThat(message.nodeId()).isEqualTo(portId);
        assertThat(message.status()).isEqualTo(NodeStatus.DISRUPTED);
        assertThat(message.operationalStatus()).isEqualTo(PortOperationalStatus.CLOSED);

        Port reloaded = portRepository.findById(portId).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(NodeStatus.DISRUPTED);
        assertThat(reloaded.getOperationalStatus()).isEqualTo(PortOperationalStatus.CLOSED);
    }

    @Test
    void malformedShipmentEventIsDroppedWithoutBreakingTheConsumer() throws Exception {
        producer.send(new ProducerRecord<>("shipment-events", "not-json", "{not valid json")).get(5, TimeUnit.SECONDS);

        // A well-formed event sent right after must still be processed: proves the bad record
        // was dropped rather than stalling/poisoning the listener.
        StompSession session = connectStomp();
        BlockingQueue<ShipmentUpdateMessage> received = new LinkedBlockingQueue<>();
        session.subscribe("/topic/shipments", frameHandlerFor(ShipmentUpdateMessage.class, received));

        String json = """
                {"shipmentId":%d,"routeId":%d,"originId":%d,"destinationId":%d,\
                "fromStatus":"PLANNED","toStatus":"IN_TRANSIT","quantity":50,\
                "occurredAt":"%s"}\
                """.formatted(shipment.getId(), shipment.getRoute().getId(), shipment.getOrigin().getId(),
                shipment.getDestination().getId(), Instant.now());
        producer.send(new ProducerRecord<>("shipment-events", String.valueOf(shipment.getId()), json)).get(5, TimeUnit.SECONDS);

        ShipmentUpdateMessage message = received.poll(10, TimeUnit.SECONDS);
        assertThat(message).isNotNull();
        assertThat(message.status()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    }

    private StompSession connectStomp() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        stompClient.setMessageConverter(converter);
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.afterPropertiesSet();
        stompClient.setTaskScheduler(taskScheduler);

        return stompClient
                .connectAsync("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {
                })
                .get(5, TimeUnit.SECONDS);
    }

    private <T> StompFrameHandler frameHandlerFor(Class<T> type, BlockingQueue<T> queue) {
        return new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return type;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.add(type.cast(payload));
            }
        };
    }
}
