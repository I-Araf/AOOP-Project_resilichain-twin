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
@Table(name = "disruption")
public class Disruption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "target_node_id")
    private NetworkNode targetNode;

    @ManyToOne
    @JoinColumn(name = "target_route_id")
    private Route targetRoute;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisruptionSeverity severity;

    @Column(name = "duration_hours", nullable = false)
    private int durationHours;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    protected Disruption() {
        // required by Hibernate
    }

    public Disruption(NetworkNode targetNode, Route targetRoute, DisruptionSeverity severity,
                       int durationHours, Instant startTime) {
        requireExactlyOneTarget(targetNode, targetRoute);
        this.targetNode = targetNode;
        this.targetRoute = targetRoute;
        this.severity = Guard.notNull(severity, "severity");
        this.durationHours = Guard.positive(durationHours, "durationHours");
        this.startTime = Guard.notNull(startTime, "startTime");
    }

    public static Disruption forNode(NetworkNode targetNode, DisruptionSeverity severity,
                                      int durationHours, Instant startTime) {
        return new Disruption(targetNode, null, severity, durationHours, startTime);
    }

    public static Disruption forRoute(Route targetRoute, DisruptionSeverity severity,
                                       int durationHours, Instant startTime) {
        return new Disruption(null, targetRoute, severity, durationHours, startTime);
    }

    private static void requireExactlyOneTarget(NetworkNode targetNode, Route targetRoute) {
        if ((targetNode == null) == (targetRoute == null)) {
            throw new IllegalArgumentException("exactly one of targetNode or targetRoute must be set");
        }
    }

    public Long getId() {
        return id;
    }

    public NetworkNode getTargetNode() {
        return targetNode;
    }

    public Route getTargetRoute() {
        return targetRoute;
    }

    public DisruptionSeverity getSeverity() {
        return severity;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Disruption other = (Disruption) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
