package com.resilichain.api.api.dto;

import com.resilichain.api.domain.Role;
import com.resilichain.api.domain.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
