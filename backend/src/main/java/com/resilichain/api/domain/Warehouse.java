package com.resilichain.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "warehouse")
@DiscriminatorValue("WAREHOUSE")
public class Warehouse extends NetworkNode {

    @Column(nullable = false)
    private int capacity;

    @Column(name = "current_stock", nullable = false)
    private int currentStock;

    protected Warehouse() {
        super();
    }

    public Warehouse(String name, double latitude, double longitude, NodeStatus status,
                      int capacity, int currentStock) {
        super(name, latitude, longitude, status);
        this.capacity = Guard.positive(capacity, "capacity");
        this.currentStock = validateCurrentStock(currentStock, this.capacity);
    }

    private static int validateCurrentStock(int currentStock, int capacity) {
        Guard.nonNegative(currentStock, "currentStock");
        if (currentStock > capacity) {
            throw new IllegalArgumentException("currentStock must not exceed capacity");
        }
        return currentStock;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        Guard.positive(capacity, "capacity");
        if (capacity < this.currentStock) {
            throw new IllegalArgumentException("capacity must not be less than currentStock");
        }
        this.capacity = capacity;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = validateCurrentStock(currentStock, this.capacity);
    }
}
