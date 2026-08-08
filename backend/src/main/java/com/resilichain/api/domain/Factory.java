package com.resilichain.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "factory")
@DiscriminatorValue("FACTORY")
public class Factory extends NetworkNode {

    @Column(name = "production_capacity", nullable = false)
    private int productionCapacity;

    @Column(name = "current_output", nullable = false)
    private int currentOutput;

    protected Factory() {
        super();
    }

    public Factory(String name, double latitude, double longitude, NodeStatus status,
                    int productionCapacity, int currentOutput) {
        super(name, latitude, longitude, status);
        this.productionCapacity = Guard.positive(productionCapacity, "productionCapacity");
        this.currentOutput = validateCurrentOutput(currentOutput, this.productionCapacity);
    }

    private static int validateCurrentOutput(int currentOutput, int productionCapacity) {
        Guard.nonNegative(currentOutput, "currentOutput");
        if (currentOutput > productionCapacity) {
            throw new IllegalArgumentException("currentOutput must not exceed productionCapacity");
        }
        return currentOutput;
    }

    public int getProductionCapacity() {
        return productionCapacity;
    }

    public void setProductionCapacity(int productionCapacity) {
        Guard.positive(productionCapacity, "productionCapacity");
        if (productionCapacity < this.currentOutput) {
            throw new IllegalArgumentException("productionCapacity must not be less than currentOutput");
        }
        this.productionCapacity = productionCapacity;
    }

    public int getCurrentOutput() {
        return currentOutput;
    }

    public void setCurrentOutput(int currentOutput) {
        this.currentOutput = validateCurrentOutput(currentOutput, this.productionCapacity);
    }
}
