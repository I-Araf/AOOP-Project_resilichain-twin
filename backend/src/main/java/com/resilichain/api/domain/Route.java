package com.resilichain.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "route")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "origin_id", nullable = false)
    private NetworkNode origin;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private NetworkNode destination;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cost;

    @Column(name = "lead_time_hours", nullable = false)
    private int leadTimeHours;

    @Column(nullable = false)
    private int capacity;

    protected Route() {
        // required by Hibernate
    }

    public Route(NetworkNode origin, NetworkNode destination, BigDecimal cost, int leadTimeHours, int capacity) {
        this.origin = Guard.notNull(origin, "origin");
        this.destination = Guard.notNull(destination, "destination");
        requireDistinctEndpoints(this.origin, this.destination);
        this.cost = Guard.nonNegative(cost, "cost");
        this.leadTimeHours = Guard.nonNegative(leadTimeHours, "leadTimeHours");
        this.capacity = Guard.positive(capacity, "capacity");
    }

    private static void requireDistinctEndpoints(NetworkNode origin, NetworkNode destination) {
        if (origin.equals(destination)) {
            throw new IllegalArgumentException("origin and destination must be different nodes");
        }
    }

    public Long getId() {
        return id;
    }

    public NetworkNode getOrigin() {
        return origin;
    }

    public void setOrigin(NetworkNode origin) {
        Guard.notNull(origin, "origin");
        requireDistinctEndpoints(origin, this.destination);
        this.origin = origin;
    }

    public NetworkNode getDestination() {
        return destination;
    }

    public void setDestination(NetworkNode destination) {
        Guard.notNull(destination, "destination");
        requireDistinctEndpoints(this.origin, destination);
        this.destination = destination;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = Guard.nonNegative(cost, "cost");
    }

    public int getLeadTimeHours() {
        return leadTimeHours;
    }

    public void setLeadTimeHours(int leadTimeHours) {
        this.leadTimeHours = Guard.nonNegative(leadTimeHours, "leadTimeHours");
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = Guard.positive(capacity, "capacity");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Route other = (Route) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
