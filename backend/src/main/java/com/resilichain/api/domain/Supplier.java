package com.resilichain.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "supplier")
@DiscriminatorValue("SUPPLIER")
public class Supplier extends NetworkNode {

    @Column(name = "reliability_rating", nullable = false)
    private double reliabilityRating;

    @Column(name = "lead_time_capability_days", nullable = false)
    private int leadTimeCapabilityDays;

    protected Supplier() {
        super();
    }

    public Supplier(String name, double latitude, double longitude, NodeStatus status,
                     double reliabilityRating, int leadTimeCapabilityDays) {
        super(name, latitude, longitude, status);
        this.reliabilityRating = Guard.inRange(reliabilityRating, 0.0, 1.0, "reliabilityRating");
        this.leadTimeCapabilityDays = Guard.nonNegative(leadTimeCapabilityDays, "leadTimeCapabilityDays");
    }

    public double getReliabilityRating() {
        return reliabilityRating;
    }

    public void setReliabilityRating(double reliabilityRating) {
        this.reliabilityRating = Guard.inRange(reliabilityRating, 0.0, 1.0, "reliabilityRating");
    }

    public int getLeadTimeCapabilityDays() {
        return leadTimeCapabilityDays;
    }

    public void setLeadTimeCapabilityDays(int leadTimeCapabilityDays) {
        this.leadTimeCapabilityDays = Guard.nonNegative(leadTimeCapabilityDays, "leadTimeCapabilityDays");
    }
}
