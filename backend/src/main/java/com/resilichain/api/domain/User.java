package com.resilichain.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.regex.Pattern;

@Entity
@Table(name = "app_user")
public class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active;

    protected User() {
        // required by Hibernate
    }

    public User(String name, String email, String passwordHash, Role role) {
        this.name = Guard.notBlank(name, "name");
        this.email = validateEmail(email);
        this.passwordHash = Guard.notBlank(passwordHash, "passwordHash");
        this.role = Guard.notNull(role, "role");
        this.active = true;
    }

    private static String validateEmail(String email) {
        Guard.notBlank(email, "email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("email must be a valid email address");
        }
        return email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Guard.notBlank(name, "name");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = validateEmail(email);
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = Guard.notBlank(passwordHash, "passwordHash");
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = Guard.notNull(role, "role");
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User other = (User) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
