package com.resilichain.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import java.time.Instant;

@Entity
@Table(name = "network_node")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "node_type", discriminatorType = DiscriminatorType.STRING)
public abstract class NetworkNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NodeStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected NetworkNode() {
        // required by Hibernate
    }

    protected NetworkNode(String name, double latitude, double longitude, NodeStatus status) {
        this.name = Guard.notBlank(name, "name");
        this.latitude = Guard.inRange(latitude, -90, 90, "latitude");
        this.longitude = Guard.inRange(longitude, -180, 180, "longitude");
        this.status = Guard.notNull(status, "status");
        this.createdAt = Instant.now();
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = Guard.inRange(latitude, -90, 90, "latitude");
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = Guard.inRange(longitude, -180, 180, "longitude");
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = Guard.notNull(status, "status");
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetworkNode other = (NetworkNode) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
