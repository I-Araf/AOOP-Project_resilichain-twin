package com.resilichain.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "port")
@DiscriminatorValue("PORT")
public class Port extends NetworkNode {

    @Column(name = "throughput_capacity", nullable = false)
    private int throughputCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "operational_status", nullable = false, length = 20)
    private PortOperationalStatus operationalStatus;

    protected Port() {
        super();
    }

    public Port(String name, double latitude, double longitude, NodeStatus status,
                int throughputCapacity, PortOperationalStatus operationalStatus) {
        super(name, latitude, longitude, status);
        this.throughputCapacity = Guard.positive(throughputCapacity, "throughputCapacity");
        this.operationalStatus = Guard.notNull(operationalStatus, "operationalStatus");
    }

    public int getThroughputCapacity() {
        return throughputCapacity;
    }

    public void setThroughputCapacity(int throughputCapacity) {
        this.throughputCapacity = Guard.positive(throughputCapacity, "throughputCapacity");
    }

    public PortOperationalStatus getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(PortOperationalStatus operationalStatus) {
        this.operationalStatus = Guard.notNull(operationalStatus, "operationalStatus");
    }
}
