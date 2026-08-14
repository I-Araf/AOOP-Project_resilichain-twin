package com.resilichain.simulator.scenario;

import com.resilichain.simulator.event.DisruptionEvent;
import com.resilichain.simulator.event.ShipmentEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * In-memory mirror of the frozen Port Chattogram scenario seeded into Postgres by
 * backend/src/main/resources/db/migration/V3__seed_chattogram_network.sql — same 12 shipment
 * ids, route/origin/destination ids and quantities, all starting PLANNED. The simulator advances
 * this local copy and emits an event per change; it never talks to the database directly.
 */
public final class ChattogramScenario {

    static final long PORT_OF_CHATTOGRAM_ID = 5;
    static final String PORT_OF_CHATTOGRAM_NAME = "Port of Chattogram";

    private final List<SimulatedShipment> shipments;
    private final Random random;

    public ChattogramScenario(Random random) {
        this.random = random;
        this.shipments = seedShipments();
    }

    private static List<SimulatedShipment> seedShipments() {
        return List.of(
                new SimulatedShipment(1, 5, 3, 5, 420),
                new SimulatedShipment(2, 5, 3, 5, 380),
                new SimulatedShipment(3, 6, 4, 5, 510),
                new SimulatedShipment(4, 6, 4, 5, 275),
                new SimulatedShipment(5, 7, 5, 6, 300),
                new SimulatedShipment(6, 7, 5, 6, 260),
                new SimulatedShipment(7, 7, 5, 6, 190),
                new SimulatedShipment(8, 8, 5, 7, 640),
                new SimulatedShipment(9, 8, 5, 7, 355),
                new SimulatedShipment(10, 8, 5, 7, 480),
                new SimulatedShipment(11, 9, 5, 8, 220),
                new SimulatedShipment(12, 9, 5, 8, 175)
        );
    }

    /** Advances one random non-terminal shipment by one legal transition and returns the resulting event, if any changed. */
    public Optional<ShipmentEvent> tick() {
        List<SimulatedShipment> eligible = shipments.stream().filter(s -> !s.isTerminal()).toList();
        if (eligible.isEmpty()) {
            return Optional.empty();
        }

        SimulatedShipment chosen = eligible.get(random.nextInt(eligible.size()));
        SimStatus from = chosen.status();
        SimStatus to = nextStatus(from, random.nextDouble());
        if (to == from) {
            return Optional.empty();
        }

        chosen.setStatus(to);
        return Optional.of(new ShipmentEvent(chosen.id(), chosen.routeId(), chosen.originId(),
                chosen.destinationId(), from, to, chosen.quantity(), Instant.now()));
    }

    private static SimStatus nextStatus(SimStatus current, double roll) {
        return switch (current) {
            case PLANNED -> SimStatus.IN_TRANSIT;
            case IN_TRANSIT -> roll < 0.2 ? SimStatus.DELAYED : roll < 0.5 ? SimStatus.DELIVERED : SimStatus.IN_TRANSIT;
            case DELAYED -> roll < 0.5 ? SimStatus.IN_TRANSIT : SimStatus.REROUTED;
            case REROUTED -> roll < 0.4 ? SimStatus.IN_TRANSIT : roll < 0.8 ? SimStatus.DELIVERED : SimStatus.DELAYED;
            case DELIVERED, CANCELLED -> current;
        };
    }

    public DisruptionEvent disruptPort(String severity, int durationHours) {
        String description = "Port of Chattogram disrupted — simulated " + severity + " severity closure for "
                + durationHours + " hours";
        return new DisruptionEvent(PORT_OF_CHATTOGRAM_ID, PORT_OF_CHATTOGRAM_NAME, severity, durationHours,
                description, Instant.now());
    }

    public String statusSummary() {
        StringBuilder summary = new StringBuilder();
        for (SimStatus status : SimStatus.values()) {
            long count = shipments.stream().filter(s -> s.status() == status).count();
            if (count > 0) {
                if (!summary.isEmpty()) {
                    summary.append(", ");
                }
                summary.append(count).append(' ').append(status);
            }
        }
        return summary.toString();
    }
}
