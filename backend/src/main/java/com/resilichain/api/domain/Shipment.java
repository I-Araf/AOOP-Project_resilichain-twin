package com.resilichain.api.domain;

import com.resilichain.api.pattern.state.ShipmentState;
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
import java.util.function.UnaryOperator;

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

    /** Planned -> InTransit. */
    public void startTransit() {
        transition(ShipmentState::start);
    }

    /** InTransit/Rerouted -> Delayed. */
    public void markDelayed() {
        transition(ShipmentState::delay);
    }

    /** Delayed/Rerouted -> InTransit (recovered without changing route). */
    public void resumeTransit() {
        transition(ShipmentState::resumeTransit);
    }

    /** Delayed -> Rerouted. */
    public void reroute() {
        transition(ShipmentState::reroute);
    }

    /** InTransit/Rerouted -> Delivered. */
    public void markDelivered() {
        transition(ShipmentState::deliver);
    }

    /** Planned/InTransit/Delayed/Rerouted -> Cancelled. */
    public void cancel() {
        transition(ShipmentState::cancel);
    }

    private void transition(UnaryOperator<ShipmentState> transitionFn) {
        ShipmentState next = transitionFn.apply(ShipmentState.of(this.status));
        this.status = next.status();
    }

    /**
     * Applies whichever named transition reaches the given target status from the shipment's
     * current status (e.g. IN_TRANSIT means startTransit() from PLANNED but resumeTransit() from
     * DELAYED/REROUTED). For an external event source (e.g. a Kafka consumer) that only knows the
     * status a shipment reached, not which action produced it. Throws IllegalStateException via
     * the same guarded transitions as the named methods if the target isn't reachable from here.
     */
    public void advanceTo(ShipmentStatus toStatus) {
        switch (toStatus) {
            case IN_TRANSIT -> {
                if (status == ShipmentStatus.PLANNED) {
                    startTransit();
                } else {
                    resumeTransit();
                }
            }
            case DELAYED -> markDelayed();
            case REROUTED -> reroute();
            case DELIVERED -> markDelivered();
            case CANCELLED -> cancel();
            case PLANNED -> throw new IllegalStateException("PLANNED is not a valid transition target");
        }
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
