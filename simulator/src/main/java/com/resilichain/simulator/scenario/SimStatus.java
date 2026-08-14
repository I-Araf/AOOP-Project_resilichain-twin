package com.resilichain.simulator.scenario;

/**
 * Mirrors the backend's ShipmentStatus values. Kept as an independent copy rather than a
 * dependency on the backend module: the simulator only emits events on Kafka, it never touches
 * the database directly, so it doesn't need (or want) a compile-time coupling to the API module.
 */
public enum SimStatus {
    PLANNED,
    IN_TRANSIT,
    DELAYED,
    REROUTED,
    DELIVERED,
    CANCELLED
}
