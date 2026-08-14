package com.resilichain.api.pattern.state;

import com.resilichain.api.domain.ShipmentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShipmentStateTest {

    @Test
    void ofReturnsTheMatchingSingletonForEveryStatus() {
        assertThat(ShipmentState.of(ShipmentStatus.PLANNED)).isSameAs(PlannedState.INSTANCE);
        assertThat(ShipmentState.of(ShipmentStatus.IN_TRANSIT)).isSameAs(InTransitState.INSTANCE);
        assertThat(ShipmentState.of(ShipmentStatus.DELAYED)).isSameAs(DelayedState.INSTANCE);
        assertThat(ShipmentState.of(ShipmentStatus.REROUTED)).isSameAs(ReroutedState.INSTANCE);
        assertThat(ShipmentState.of(ShipmentStatus.DELIVERED)).isSameAs(DeliveredState.INSTANCE);
        assertThat(ShipmentState.of(ShipmentStatus.CANCELLED)).isSameAs(CancelledState.INSTANCE);
    }

    @Test
    void plannedAllowsStartOrCancelOnly() {
        ShipmentState planned = PlannedState.INSTANCE;

        assertThat(planned.start()).isSameAs(InTransitState.INSTANCE);
        assertThat(planned.cancel()).isSameAs(CancelledState.INSTANCE);
        assertThrows(IllegalStateException.class, planned::delay);
        assertThrows(IllegalStateException.class, planned::reroute);
        assertThrows(IllegalStateException.class, planned::resumeTransit);
        assertThrows(IllegalStateException.class, planned::deliver);
    }

    @Test
    void inTransitAllowsDelayDeliverOrCancel() {
        ShipmentState inTransit = InTransitState.INSTANCE;

        assertThat(inTransit.delay()).isSameAs(DelayedState.INSTANCE);
        assertThat(inTransit.deliver()).isSameAs(DeliveredState.INSTANCE);
        assertThat(inTransit.cancel()).isSameAs(CancelledState.INSTANCE);
        assertThrows(IllegalStateException.class, inTransit::start);
        assertThrows(IllegalStateException.class, inTransit::reroute);
        assertThrows(IllegalStateException.class, inTransit::resumeTransit);
    }

    @Test
    void delayedAllowsResumeRerouteOrCancel() {
        ShipmentState delayed = DelayedState.INSTANCE;

        assertThat(delayed.resumeTransit()).isSameAs(InTransitState.INSTANCE);
        assertThat(delayed.reroute()).isSameAs(ReroutedState.INSTANCE);
        assertThat(delayed.cancel()).isSameAs(CancelledState.INSTANCE);
        assertThrows(IllegalStateException.class, delayed::start);
        assertThrows(IllegalStateException.class, delayed::deliver);
    }

    @Test
    void reroutedAllowsResumeDelayDeliverOrCancel() {
        ShipmentState rerouted = ReroutedState.INSTANCE;

        assertThat(rerouted.resumeTransit()).isSameAs(InTransitState.INSTANCE);
        assertThat(rerouted.delay()).isSameAs(DelayedState.INSTANCE);
        assertThat(rerouted.deliver()).isSameAs(DeliveredState.INSTANCE);
        assertThat(rerouted.cancel()).isSameAs(CancelledState.INSTANCE);
        assertThrows(IllegalStateException.class, rerouted::start);
        assertThrows(IllegalStateException.class, rerouted::reroute);
    }

    @Test
    void deliveredIsTerminal() {
        ShipmentState delivered = DeliveredState.INSTANCE;

        assertThrows(IllegalStateException.class, delivered::start);
        assertThrows(IllegalStateException.class, delivered::delay);
        assertThrows(IllegalStateException.class, delivered::resumeTransit);
        assertThrows(IllegalStateException.class, delivered::reroute);
        assertThrows(IllegalStateException.class, delivered::deliver);
        assertThrows(IllegalStateException.class, delivered::cancel);
    }

    @Test
    void cancelledIsTerminal() {
        ShipmentState cancelled = CancelledState.INSTANCE;

        assertThrows(IllegalStateException.class, cancelled::start);
        assertThrows(IllegalStateException.class, cancelled::delay);
        assertThrows(IllegalStateException.class, cancelled::resumeTransit);
        assertThrows(IllegalStateException.class, cancelled::reroute);
        assertThrows(IllegalStateException.class, cancelled::deliver);
        assertThrows(IllegalStateException.class, cancelled::cancel);
    }
}
