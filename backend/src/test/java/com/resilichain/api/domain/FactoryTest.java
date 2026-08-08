package com.resilichain.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FactoryTest {

    @Test
    void constructingWithValidValuesSucceeds() {
        Factory factory = new Factory("Dhaka Plant", 23.8, 90.4, NodeStatus.OPERATIONAL, 1000, 400);

        assertThat(factory.getProductionCapacity()).isEqualTo(1000);
        assertThat(factory.getCurrentOutput()).isEqualTo(400);
    }

    @Test
    void zeroOrNegativeProductionCapacityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Factory("A", 0, 0, NodeStatus.OPERATIONAL, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new Factory("A", 0, 0, NodeStatus.OPERATIONAL, -100, 0));
    }

    @Test
    void negativeCurrentOutputThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Factory("A", 0, 0, NodeStatus.OPERATIONAL, 100, -1));
    }

    @Test
    void currentOutputExceedingCapacityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Factory("A", 0, 0, NodeStatus.OPERATIONAL, 100, 101));
    }

    @Test
    void currentOutputEqualToCapacityIsAllowed() {
        Factory factory = new Factory("A", 0, 0, NodeStatus.OPERATIONAL, 100, 100);
        assertThat(factory.getCurrentOutput()).isEqualTo(100);
    }

    @Test
    void setCurrentOutputEnforcesInvariantAfterConstruction() {
        Factory factory = new Factory("A", 0, 0, NodeStatus.OPERATIONAL, 100, 50);
        assertThrows(IllegalArgumentException.class, () -> factory.setCurrentOutput(101));
    }

    @Test
    void setProductionCapacityRejectsValueBelowCurrentOutput() {
        Factory factory = new Factory("A", 0, 0, NodeStatus.OPERATIONAL, 100, 80);
        assertThrows(IllegalArgumentException.class, () -> factory.setProductionCapacity(50));
    }
}
