package com.resilichain.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @Test
    void constructingWithValidValuesSucceeds() {
        User user = new User("Alice Planner", "alice@resilichain.com", "hashed-value", Role.PLANNER);

        assertThat(user.getName()).isEqualTo("Alice Planner");
        assertThat(user.getEmail()).isEqualTo("alice@resilichain.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashed-value");
        assertThat(user.getRole()).isEqualTo(Role.PLANNER);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void blankNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("  ", "alice@resilichain.com", "hash", Role.PLANNER));
    }

    @Test
    void blankEmailThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Alice", " ", "hash", Role.PLANNER));
    }

    @Test
    void invalidEmailFormatThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Alice", "not-an-email", "hash", Role.PLANNER));
    }

    @Test
    void blankPasswordHashThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Alice", "alice@resilichain.com", " ", Role.PLANNER));
    }

    @Test
    void nullRoleThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Alice", "alice@resilichain.com", "hash", null));
    }

    @Test
    void equalsIsIdBasedAndTransientUsersAreNeverEqual() {
        User a = new User("Alice", "alice@resilichain.com", "hash", Role.PLANNER);
        User b = new User("Alice", "alice@resilichain.com", "hash", Role.PLANNER);

        assertThat(a).isEqualTo(a);
        assertThat(a).isNotEqualTo(b);
    }
}
