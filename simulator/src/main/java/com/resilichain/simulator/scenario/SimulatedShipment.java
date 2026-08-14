package com.resilichain.simulator.scenario;

final class SimulatedShipment {

    private final long id;
    private final long routeId;
    private final long originId;
    private final long destinationId;
    private final int quantity;
    private SimStatus status;

    SimulatedShipment(long id, long routeId, long originId, long destinationId, int quantity) {
        this.id = id;
        this.routeId = routeId;
        this.originId = originId;
        this.destinationId = destinationId;
        this.quantity = quantity;
        this.status = SimStatus.PLANNED;
    }

    long id() {
        return id;
    }

    long routeId() {
        return routeId;
    }

    long originId() {
        return originId;
    }

    long destinationId() {
        return destinationId;
    }

    int quantity() {
        return quantity;
    }

    SimStatus status() {
        return status;
    }

    void setStatus(SimStatus status) {
        this.status = status;
    }

    boolean isTerminal() {
        return status == SimStatus.DELIVERED || status == SimStatus.CANCELLED;
    }
}
