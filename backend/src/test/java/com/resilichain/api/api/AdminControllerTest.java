package com.resilichain.api.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resilichain.api.api.dto.CreateUserRequest;
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

import static org.springframework.security.core.userdetails.User.withUsername;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AdminControllerTest {

    private static final String ADMIN_EMAIL = "admin-controller-it@resilichain.com";
    private static final String PLANNER_EMAIL = "planner-controller-it@resilichain.com";

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
    void adminCanCreateAndListUsers() throws Exception {
        String adminToken = tokenFor(ADMIN_EMAIL, "ADMIN");
        CreateUserRequest request = new CreateUserRequest("New Operator", "new-operator-it@resilichain.com",
                "SomePassword123!", Role.OPERATOR);

        mockMvc.perform(post("/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new-operator-it@resilichain.com"))
                .andExpect(jsonPath("$.role").value("OPERATOR"));

        mockMvc.perform(get("/admin/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void nonAdminCannotCreateUsers() throws Exception {
        String plannerToken = tokenFor(PLANNER_EMAIL, "PLANNER");
        CreateUserRequest request = new CreateUserRequest("Blocked", "blocked-it@resilichain.com",
                "SomePassword123!", Role.OPERATOR);

        mockMvc.perform(post("/admin/users")
                        .header("Authorization", "Bearer " + plannerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized());
    }
}
