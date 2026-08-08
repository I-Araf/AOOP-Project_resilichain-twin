package com.resilichain.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-at-least-32-bytes-long-for-hs256";

    private final UserDetails userDetails = User.withUsername("planner@resilichain.com")
            .password("irrelevant")
            .authorities("ROLE_PLANNER")
            .build();

    private JwtService serviceWith(String secret, long expirationMinutes) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setExpirationMinutes(expirationMinutes);
        return new JwtService(properties);
    }

    @Test
    void generateThenExtractUsernameRoundTrips() {
        JwtService jwtService = serviceWith(SECRET, 60);

        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractUsername(token)).isEqualTo("planner@resilichain.com");
    }

    @Test
    void tokenIsValidForTheUserItWasIssuedTo() {
        JwtService jwtService = serviceWith(SECRET, 60);

        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void tokenIsInvalidForADifferentUser() {
        JwtService jwtService = serviceWith(SECRET, 60);
        UserDetails otherUser = User.withUsername("other@resilichain.com")
                .password("irrelevant")
                .authorities("ROLE_OPERATOR")
                .build();

        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void expiredTokenIsRejected() throws InterruptedException {
        JwtService jwtService = serviceWith(SECRET, 0);

        String token = jwtService.generateToken(userDetails);
        Thread.sleep(50);

        assertThat(jwtService.isTokenValid(token, userDetails)).isFalse();
    }

    @Test
    void tokenWithTamperedSignatureIsRejected() {
        JwtService issuer = serviceWith(SECRET, 60);
        JwtService verifierWithDifferentSecret = serviceWith("different-secret-key-at-least-32-bytes-long!!", 60);

        String token = issuer.generateToken(userDetails);

        assertThat(verifierWithDifferentSecret.isTokenValid(token, userDetails)).isFalse();
    }
}
