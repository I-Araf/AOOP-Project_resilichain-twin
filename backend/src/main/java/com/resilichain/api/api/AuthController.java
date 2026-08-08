package com.resilichain.api.api;

import com.resilichain.api.api.dto.LoginRequest;
import com.resilichain.api.api.dto.LoginResponse;
import com.resilichain.api.api.dto.UserResponse;
import com.resilichain.api.security.AuthenticatedUser;
import com.resilichain.api.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        String token = jwtService.generateToken(authenticatedUser);

        return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds(),
                UserResponse.from(authenticatedUser.getUser()));
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return UserResponse.from(authenticatedUser.getUser());
    }
}
