package com.resilichain.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SupplierTest {

    @Test
    void constructingWithValidValuesSucceeds() {
        Supplier supplier = new Supplier("Acme Textiles", 22.3, 91.8, NodeStatus.OPERATIONAL, 0.85, 5);

        assertThat(supplier.getReliabilityRating()).isEqualTo(0.85);
        assertThat(supplier.getLeadTimeCapabilityDays()).isEqualTo(5);
    }

    @Test
    void reliabilityRatingBelowZeroThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Supplier("A", 0, 0, NodeStatus.OPERATIONAL, -0.01, 5));
    }

    @Test
    void reliabilityRatingAboveOneThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Supplier("A", 0, 0, NodeStatus.OPERATIONAL, 1.01, 5));
    }

    @Test
    void reliabilityRatingAtBoundsIsAllowed() {
        assertThat(new Supplier("A", 0, 0, NodeStatus.OPERATIONAL, 0.0, 5).getReliabilityRating()).isEqualTo(0.0);
        assertThat(new Supplier("A", 0, 0, NodeStatus.OPERATIONAL, 1.0, 5).getReliabilityRating()).isEqualTo(1.0);
    }

    @Test
    void negativeLeadTimeCapabilityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Supplier("A", 0, 0, NodeStatus.OPERATIONAL, 0.5, -1));
    }
}
