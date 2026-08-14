package com.resilichain.api.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resilichain.api.api.dto.RouteRequest;
import com.resilichain.api.api.dto.SupplierRequest;
import com.resilichain.api.api.dto.WarehouseRequest;
import com.resilichain.api.domain.NodeStatus;
import com.resilichain.api.domain.Role;
import com.resilichain.api.domain.User;
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

import static org.springframework.security.core.userdetails.User.withUsername;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class NetworkControllerTest {

    private static final String ADMIN_EMAIL = "admin-network-it@resilichain.com";
    private static final String PLANNER_EMAIL = "planner-network-it@resilichain.com";

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

    @BeforeEach
    void seedUsers() {
        userRepository.findByEmail(ADMIN_EMAIL).ifPresentOrElse(u -> { }, () ->
                userRepository.save(new User("Admin", ADMIN_EMAIL, passwordEncoder.encode("irrelevant"), Role.ADMIN)));
        userRepository.findByEmail(PLANNER_EMAIL).ifPresentOrElse(u -> { }, () ->
                userRepository.save(new User("Planner", PLANNER_EMAIL, passwordEncoder.encode("irrelevant"), Role.PLANNER)));
    }

    private String tokenFor(String email, String role) {
        UserBuilder springUser = withUsername(email).password("irrelevant").authorities("ROLE_" + role);
        return jwtService.generateToken(springUser.build());
    }

    @Test
    void adminCanCreateSupplierAndPlannerCanListIt() throws Exception {
        String adminToken = tokenFor(ADMIN_EMAIL, "ADMIN");
        String plannerToken = tokenFor(PLANNER_EMAIL, "PLANNER");
        SupplierRequest request = new SupplierRequest("Acme Textiles", 22.35, 91.83, NodeStatus.OPERATIONAL, 0.9, 5);

        mockMvc.perform(post("/network/suppliers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme Textiles"))
                .andExpect(jsonPath("$.id").isNotEmpty());

        mockMvc.perform(get("/network/suppliers").header("Authorization", "Bearer " + plannerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Acme Textiles')]").exists());
    }

    @Test
    void plannerCannotCreateNetworkData() throws Exception {
        String plannerToken = tokenFor(PLANNER_EMAIL, "PLANNER");
        WarehouseRequest request = new WarehouseRequest("Chattogram DC", 22.33, 91.80, NodeStatus.OPERATIONAL, 1000, 200);

        mockMvc.perform(post("/network/warehouses")
                        .header("Authorization", "Bearer " + plannerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void routeCreationLinksTwoExistingNodes() throws Exception {
        String adminToken = tokenFor(ADMIN_EMAIL, "ADMIN");

        SupplierRequest supplierRequest = new SupplierRequest("Origin Supplier", 22.35, 91.83, NodeStatus.OPERATIONAL, 0.9, 5);
        String supplierJson = mockMvc.perform(post("/network/suppliers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(supplierRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long originId = objectMapper.readTree(supplierJson).get("id").asLong();

        WarehouseRequest warehouseRequest = new WarehouseRequest("Destination Warehouse", 22.33, 91.80, NodeStatus.OPERATIONAL, 1000, 200);
        String warehouseJson = mockMvc.perform(post("/network/warehouses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(warehouseRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long destinationId = objectMapper.readTree(warehouseJson).get("id").asLong();

        RouteRequest routeRequest = new RouteRequest(originId, destinationId, new BigDecimal("150.00"), 24, 500);

        mockMvc.perform(post("/network/routes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(routeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originName").value("Origin Supplier"))
                .andExpect(jsonPath("$.destinationName").value("Destination Warehouse"));
    }

    @Test
    void routeCreationWithUnknownNodeReturnsNotFound() throws Exception {
        String adminToken = tokenFor(ADMIN_EMAIL, "ADMIN");
        RouteRequest routeRequest = new RouteRequest(999999L, 999998L, new BigDecimal("10.00"), 1, 1);

        mockMvc.perform(post("/network/routes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(routeRequest)))
                .andExpect(status().isNotFound());
    }
}
