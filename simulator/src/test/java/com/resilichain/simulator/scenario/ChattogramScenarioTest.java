package com.resilichain.simulator.scenario;

import com.resilichain.simulator.event.DisruptionEvent;
import com.resilichain.simulator.event.ShipmentEvent;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class ChattogramScenarioTest {

    @Test
    void tickAlwaysStartsAPlannedShipmentMovingToInTransitFirst() {
        // roll doesn't matter for the PLANNED->IN_TRANSIT branch, so seed 0 for determinism
        ChattogramScenario scenario = new ChattogramScenario(new Random(0));

        Optional<ShipmentEvent> event = scenario.tick();

        assertThat(event).isPresent();
        assertThat(event.get().fromStatus()).isEqualTo(SimStatus.PLANNED);
        assertThat(event.get().toStatus()).isEqualTo(SimStatus.IN_TRANSIT);
        assertThat(event.get().shipmentId()).isBetween(1L, 12L);
    }

    @Test
    void tickEventuallyMovesEveryShipmentToATerminalState() {
        ChattogramScenario scenario = new ChattogramScenario(new Random(42));

        for (int i = 0; i < 5000; i++) {
            scenario.tick();
        }

        String summary = scenario.statusSummary();
        assertThat(summary).doesNotContain("PLANNED");
        assertThat(summary).satisfiesAnyOf(
                s -> assertThat(s).contains("DELIVERED"),
                s -> assertThat(s).contains("CANCELLED")
        );
    }

    @Test
    void statusSummaryStartsWithAllTwelveShipmentsPlanned() {
        ChattogramScenario scenario = new ChattogramScenario(new Random(1));

        assertThat(scenario.statusSummary()).isEqualTo("12 PLANNED");
    }

    @Test
    void disruptPortReturnsPortOfChattogramWithRequestedSeverityAndDuration() {
        ChattogramScenario scenario = new ChattogramScenario(new Random(1));

        DisruptionEvent event = scenario.disruptPort("CRITICAL", 36);

        assertThat(event.nodeId()).isEqualTo(ChattogramScenario.PORT_OF_CHATTOGRAM_ID);
        assertThat(event.nodeName()).isEqualTo(ChattogramScenario.PORT_OF_CHATTOGRAM_NAME);
        assertThat(event.severity()).isEqualTo("CRITICAL");
        assertThat(event.durationHours()).isEqualTo(36);
        assertThat(event.description()).contains("Port of Chattogram");
    }
}
