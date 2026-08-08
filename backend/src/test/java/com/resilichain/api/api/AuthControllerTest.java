package com.resilichain.api.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resilichain.api.api.dto.LoginRequest;
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
class AuthControllerTest {

    private static final String ADMIN_EMAIL = "admin-it@resilichain.com";
    private static final String ADMIN_PASSWORD = "ChangeMe123!";

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
    void seedAdminUser() {
        userRepository.findByEmail(ADMIN_EMAIL).ifPresentOrElse(u -> { }, () ->
                userRepository.save(new User("IT Admin", ADMIN_EMAIL, passwordEncoder.encode(ADMIN_PASSWORD), Role.ADMIN)));
    }

    @Test
    void loginWithValidCredentialsReturnsJwt() throws Exception {
        LoginRequest request = new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value(ADMIN_EMAIL));
    }

    @Test
    void loginWithBadPasswordReturnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest(ADMIN_EMAIL, "wrong-password");

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meWithValidTokenReturnsCurrentUser() throws Exception {
        UserBuilder springUser = withUsername(ADMIN_EMAIL).password("irrelevant").authorities("ROLE_ADMIN");
        String token = jwtService.generateToken(springUser.build());

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(ADMIN_EMAIL))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void meWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
