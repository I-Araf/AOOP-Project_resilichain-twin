package com.resilichain.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WarehouseTest {

    @Test
    void constructingWithValidValuesSucceeds() {
        Warehouse warehouse = new Warehouse("Central WH", 23.7, 90.3, NodeStatus.OPERATIONAL, 5000, 3200);

        assertThat(warehouse.getCapacity()).isEqualTo(5000);
        assertThat(warehouse.getCurrentStock()).isEqualTo(3200);
    }

    @Test
    void zeroOrNegativeCapacityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Warehouse("A", 0, 0, NodeStatus.OPERATIONAL, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new Warehouse("A", 0, 0, NodeStatus.OPERATIONAL, -10, 0));
    }

    @Test
    void negativeCurrentStockThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Warehouse("A", 0, 0, NodeStatus.OPERATIONAL, 100, -1));
    }

    @Test
    void currentStockExceedingCapacityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Warehouse("A", 0, 0, NodeStatus.OPERATIONAL, 100, 101));
    }

    @Test
    void currentStockEqualToCapacityIsAllowed() {
        Warehouse warehouse = new Warehouse("A", 0, 0, NodeStatus.OPERATIONAL, 100, 100);
        assertThat(warehouse.getCurrentStock()).isEqualTo(100);
    }

    @Test
    void setCurrentStockEnforcesInvariantAfterConstruction() {
        Warehouse warehouse = new Warehouse("A", 0, 0, NodeStatus.OPERATIONAL, 100, 50);
        assertThrows(IllegalArgumentException.class, () -> warehouse.setCurrentStock(101));
    }

    @Test
    void setCapacityRejectsValueBelowCurrentStock() {
        Warehouse warehouse = new Warehouse("A", 0, 0, NodeStatus.OPERATIONAL, 100, 80);
        assertThrows(IllegalArgumentException.class, () -> warehouse.setCapacity(50));
    }
}
