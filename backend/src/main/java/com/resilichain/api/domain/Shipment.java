package com.resilichain.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "shipment")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(optional = false)
    @JoinColumn(name = "origin_id", nullable = false)
    private NetworkNode origin;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private NetworkNode destination;

    // Plain enum for Week 1; Stage 5 replaces status mutation with the State design pattern.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    @Column(nullable = false)
    private int quantity;

    @Column
    private Instant eta;

    protected Shipment() {
        // required by Hibernate
    }

    public Shipment(Route route, NetworkNode origin, NetworkNode destination, int quantity, Instant eta) {
        this.route = Guard.notNull(route, "route");
        this.origin = Guard.notNull(origin, "origin");
        this.destination = Guard.notNull(destination, "destination");
        if (this.origin.equals(this.destination)) {
            throw new IllegalArgumentException("origin and destination must be different nodes");
        }
        this.quantity = Guard.positive(quantity, "quantity");
        this.eta = eta;
        this.status = ShipmentStatus.PLANNED;
    }

    public Long getId() {
        return id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = Guard.notNull(route, "route");
    }

    public NetworkNode getOrigin() {
        return origin;
    }

    public NetworkNode getDestination() {
        return destination;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = Guard.notNull(status, "status");
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Guard.positive(quantity, "quantity");
    }

    public Instant getEta() {
        return eta;
    }

    public void setEta(Instant eta) {
        this.eta = eta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Shipment other = (Shipment) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
