package com.resilichain.api.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resilichain.api.domain.Factory;
import com.resilichain.api.domain.NodeStatus;
import com.resilichain.api.domain.Port;
import com.resilichain.api.domain.PortOperationalStatus;
import com.resilichain.api.domain.Role;
import com.resilichain.api.domain.Route;
import com.resilichain.api.domain.Shipment;
import com.resilichain.api.domain.Supplier;
import com.resilichain.api.domain.User;
import com.resilichain.api.domain.Warehouse;
import com.resilichain.api.repository.NetworkNodeRepository;
import com.resilichain.api.repository.RouteRepository;
import com.resilichain.api.repository.ShipmentRepository;
import com.resilichain.api.repository.UserRepository;
import com.resilichain.api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.springframework.security.core.userdetails.User.withUsername;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class DisruptionControllerTest {

    private static final String ADMIN_EMAIL = "admin-disruption-it@resilichain.com";
    private static final String PLANNER_EMAIL = "planner-disruption-it@resilichain.com";
    private static final String OPERATOR_EMAIL = "operator-disruption-it@resilichain.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private NetworkNodeRepository networkNodeRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @BeforeEach
    void seedUsers() {
        userRepository.findByEmail(ADMIN_EMAIL).ifPresentOrElse(u -> { }, () ->
                userRepository.save(new User("Admin", ADMIN_EMAIL, passwordEncoder.encode("irrelevant"), Role.ADMIN)));
        userRepository.findByEmail(PLANNER_EMAIL).ifPresentOrElse(u -> { }, () ->
                userRepository.save(new User("Planner", PLANNER_EMAIL, passwordEncoder.encode("irrelevant"), Role.PLANNER)));
        userRepository.findByEmail(OPERATOR_EMAIL).ifPresentOrElse(u -> { }, () ->
                userRepository.save(new User("Operator", OPERATOR_EMAIL, passwordEncoder.encode("irrelevant"), Role.OPERATOR)));
    }

    private String tokenFor(String email, String role) {
        UserBuilder springUser = withUsername(email).password("irrelevant").authorities("ROLE_" + role);
        return jwtService.generateToken(springUser.build());
    }

    /** Rebuilds the frozen Chattogram topology by hand for each test (Flyway/V3 doesn't run in the test profile). */
    private Long seedChattogramTopologyAndReturnPortId() {
        Supplier supplier1 = new Supplier("Narayanganj Textile Mills", 23.62, 90.50, NodeStatus.OPERATIONAL, 0.92, 4);
        Supplier supplier2 = new Supplier("Gazipur Yarn Suppliers", 24.00, 90.42, NodeStatus.OPERATIONAL, 0.88, 6);
        Factory factory1 = new Factory("Dhaka Garments Factory", 23.81, 90.41, NodeStatus.OPERATIONAL, 8000, 6200);
        Factory factory2 = new Factory("Chattogram EPZ Factory", 22.32, 91.78, NodeStatus.OPERATIONAL, 6000, 5100);
        Port port = new Port("Port of Chattogram", 22.34, 91.83, NodeStatus.OPERATIONAL, 9000, PortOperationalStatus.OPEN);
        Warehouse warehouse1 = new Warehouse("Chattogram Distribution Warehouse", 22.36, 91.78, NodeStatus.OPERATIONAL, 12000, 7400);
        Warehouse warehouse2 = new Warehouse("Dhaka Central Warehouse", 23.78, 90.40, NodeStatus.OPERATIONAL, 15000, 9800);
        Warehouse warehouse3 = new Warehouse("Sylhet Regional Warehouse", 24.89, 91.87, NodeStatus.OPERATIONAL, 6000, 3200);
        networkNodeRepository.saveAll(List.of(supplier1, supplier2, factory1, factory2, port,
                warehouse1, warehouse2, warehouse3));

        routeRepository.save(new Route(supplier1, factory1, new BigDecimal("1200.00"), 18, 2000));
        routeRepository.save(new Route(supplier2, factory1, new BigDecimal("900.00"), 22, 1800));
        routeRepository.save(new Route(supplier2, factory2, new BigDecimal("1500.00"), 30, 1600));
        routeRepository.save(new Route(supplier1, factory2, new BigDecimal("1700.00"), 28, 1600));
        Route r5 = routeRepository.save(new Route(factory1, port, new BigDecimal("2200.00"), 14, 3000));
        Route r6 = routeRepository.save(new Route(factory2, port, new BigDecimal("800.00"), 6, 3500));
        Route r7 = routeRepository.save(new Route(port, warehouse1, new BigDecimal("600.00"), 4, 4000));
        Route r8 = routeRepository.save(new Route(port, warehouse2, new BigDecimal("2400.00"), 16, 4000));
        Route r9 = routeRepository.save(new Route(port, warehouse3, new BigDecimal("3100.00"), 22, 2500));
        routeRepository.save(new Route(factory1, warehouse2, new BigDecimal("1900.00"), 12, 2000));

        shipmentRepository.saveAll(List.of(
                new Shipment(r5, factory1, port, 420, Instant.now()),
                new Shipment(r5, factory1, port, 380, Instant.now()),
                new Shipment(r6, factory2, port, 510, Instant.now()),
                new Shipment(r6, factory2, port, 275, Instant.now()),
                new Shipment(r7, port, warehouse1, 300, Instant.now()),
                new Shipment(r7, port, warehouse1, 260, Instant.now()),
                new Shipment(r7, port, warehouse1, 190, Instant.now()),
                new Shipment(r8, port, warehouse2, 640, Instant.now()),
                new Shipment(r8, port, warehouse2, 355, Instant.now()),
                new Shipment(r8, port, warehouse2, 480, Instant.now()),
                new Shipment(r9, port, warehouse3, 220, Instant.now()),
                new Shipment(r9, port, warehouse3, 175, Instant.now())));

        return port.getId();
    }

    @Test
    void plannerCanTriggerDisruptionAndImpactSummaryMatchesChattogramScenario() throws Exception {
        Long portId = seedChattogramTopologyAndReturnPortId();
        String plannerToken = tokenFor(PLANNER_EMAIL, "PLANNER");
        String body = objectMapper.writeValueAsString(new TriggerBody(portId, "CRITICAL", 36));

        mockMvc.perform(post("/disruptions")
                        .header("Authorization", "Bearer " + plannerToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disruptionId").isNotEmpty())
                .andExpect(jsonPath("$.affectedShipmentCount").value(12))
                .andExpect(jsonPath("$.affectedWarehouseCount").value(3));
    }

    @Test
    void adminCanAlsoTriggerDisruption() throws Exception {
        Long portId = seedChattogramTopologyAndReturnPortId();
        String adminToken = tokenFor(ADMIN_EMAIL, "ADMIN");
        String body = objectMapper.writeValueAsString(new TriggerBody(portId, "HIGH", 12));

        mockMvc.perform(post("/disruptions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void operatorCannotTriggerDisruption() throws Exception {
        Long portId = seedChattogramTopologyAndReturnPortId();
        String operatorToken = tokenFor(OPERATOR_EMAIL, "OPERATOR");
        String body = objectMapper.writeValueAsString(new TriggerBody(portId, "HIGH", 12));

        mockMvc.perform(post("/disruptions")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void triggeringDisruptionOnUnknownNodeReturnsNotFound() throws Exception {
        String adminToken = tokenFor(ADMIN_EMAIL, "ADMIN");
        String body = objectMapper.writeValueAsString(new TriggerBody(999999L, "HIGH", 12));

        mockMvc.perform(post("/disruptions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidSeverityValueReturnsBadRequest() throws Exception {
        Long portId = seedChattogramTopologyAndReturnPortId();
        String adminToken = tokenFor(ADMIN_EMAIL, "ADMIN");
        String body = "{\"targetNodeId\":" + portId + ",\"severity\":\"NOT_A_SEVERITY\",\"durationHours\":12}";

        mockMvc.perform(post("/disruptions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private record TriggerBody(Long targetNodeId, String severity, int durationHours) {
    }
}
