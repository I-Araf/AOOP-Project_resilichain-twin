package com.resilichain.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NetworkNodeInvariantsTest {

    /** Minimal concrete subclass so the abstract base's own invariants can be exercised directly. */
    private static final class TestNode extends NetworkNode {
        TestNode(String name, double latitude, double longitude, NodeStatus status) {
            super(name, latitude, longitude, status);
        }
    }

    @Test
    void constructingWithValidValuesSucceeds() {
        TestNode node = new TestNode("Chattogram Port", 22.335, 91.834, NodeStatus.OPERATIONAL);

        assertThat(node.getName()).isEqualTo("Chattogram Port");
        assertThat(node.getLatitude()).isEqualTo(22.335);
        assertThat(node.getLongitude()).isEqualTo(91.834);
        assertThat(node.getStatus()).isEqualTo(NodeStatus.OPERATIONAL);
        assertThat(node.getCreatedAt()).isNotNull();
    }

    @Test
    void blankNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new TestNode("  ", 0, 0, NodeStatus.OPERATIONAL));
    }

    @Test
    void nullNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new TestNode(null, 0, 0, NodeStatus.OPERATIONAL));
    }

    @Test
    void latitudeAtBoundsIsAllowed() {
        assertThat(new TestNode("A", 90, 0, NodeStatus.OPERATIONAL).getLatitude()).isEqualTo(90);
        assertThat(new TestNode("B", -90, 0, NodeStatus.OPERATIONAL).getLatitude()).isEqualTo(-90);
    }

    @Test
    void latitudeOutOfRangeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new TestNode("A", 90.0001, 0, NodeStatus.OPERATIONAL));
        assertThrows(IllegalArgumentException.class,
                () -> new TestNode("A", -90.0001, 0, NodeStatus.OPERATIONAL));
    }

    @Test
    void longitudeOutOfRangeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new TestNode("A", 0, 180.0001, NodeStatus.OPERATIONAL));
        assertThrows(IllegalArgumentException.class,
                () -> new TestNode("A", 0, -180.0001, NodeStatus.OPERATIONAL));
    }

    @Test
    void nullStatusThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new TestNode("A", 0, 0, null));
    }

    @Test
    void setStatusRejectsNull() {
        TestNode node = new TestNode("A", 0, 0, NodeStatus.OPERATIONAL);
        assertThrows(IllegalArgumentException.class, () -> node.setStatus(null));
    }

    @Test
    void equalsIsReflexive() {
        TestNode node = new TestNode("A", 0, 0, NodeStatus.OPERATIONAL);
        assertThat(node).isEqualTo(node);
    }

    @Test
    void transientNodesWithoutIdsAreNeverEqual() {
        TestNode a = new TestNode("A", 0, 0, NodeStatus.OPERATIONAL);
        TestNode b = new TestNode("A", 0, 0, NodeStatus.OPERATIONAL);
        assertThat(a).isNotEqualTo(b);
    }
}
