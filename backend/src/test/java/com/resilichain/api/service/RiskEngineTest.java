package com.resilichain.api.service;

import com.resilichain.api.domain.DisruptionSeverity;
import com.resilichain.api.domain.Factory;
import com.resilichain.api.domain.NetworkNode;
import com.resilichain.api.domain.NodeStatus;
import com.resilichain.api.domain.Port;
import com.resilichain.api.domain.PortOperationalStatus;
import com.resilichain.api.domain.Route;
import com.resilichain.api.domain.Shipment;
import com.resilichain.api.domain.Supplier;
import com.resilichain.api.domain.Warehouse;
import com.resilichain.api.repository.NetworkNodeRepository;
import com.resilichain.api.repository.RouteRepository;
import com.resilichain.api.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mirrors the frozen Port Chattogram topology seeded by
 * db/migration/V3__seed_chattogram_network.sql (Flyway is disabled in the test profile, so that
 * migration never runs against the test datasource - this fixture reproduces it by hand).
 */
@DataJpaTest
@Import(RiskEngine.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class RiskEngineTest {

    @Autowired
    private NetworkNodeRepository networkNodeRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private RiskEngine riskEngine;

    private Supplier supplier1;
    private Supplier supplier2;
    private Factory factory1;
    private Factory factory2;
    private Port port;
    private Warehouse warehouse1;
    private Warehouse warehouse2;
    private Warehouse warehouse3;

    @BeforeEach
    void seedChattogramTopology() {
        supplier1 = new Supplier("Narayanganj Textile Mills", 23.62, 90.50, NodeStatus.OPERATIONAL, 0.92, 4);
        supplier2 = new Supplier("Gazipur Yarn Suppliers", 24.00, 90.42, NodeStatus.OPERATIONAL, 0.88, 6);
        factory1 = new Factory("Dhaka Garments Factory", 23.81, 90.41, NodeStatus.OPERATIONAL, 8000, 6200);
        factory2 = new Factory("Chattogram EPZ Factory", 22.32, 91.78, NodeStatus.OPERATIONAL, 6000, 5100);
        port = new Port("Port of Chattogram", 22.34, 91.83, NodeStatus.OPERATIONAL, 9000, PortOperationalStatus.OPEN);
        warehouse1 = new Warehouse("Chattogram Distribution Warehouse", 22.36, 91.78, NodeStatus.OPERATIONAL, 12000, 7400);
        warehouse2 = new Warehouse("Dhaka Central Warehouse", 23.78, 90.40, NodeStatus.OPERATIONAL, 15000, 9800);
        warehouse3 = new Warehouse("Sylhet Regional Warehouse", 24.89, 91.87, NodeStatus.OPERATIONAL, 6000, 3200);
        networkNodeRepository.saveAll(List.of(supplier1, supplier2, factory1, factory2, port,
                warehouse1, warehouse2, warehouse3));

        routeRepository.save(new Route(supplier1, factory1, new BigDecimal("1200.00"), 18, 2000));
        routeRepository.save(new Route(supplier2, factory1, new BigDecimal("900.00"), 22, 1800));
        routeRepository.save(new Route(supplier2, factory2, new BigDecimal("1500.00"), 30, 1600));
        routeRepository.save(new Route(supplier1, factory2, new BigDecimal("1700.00"), 28, 1600));
        Route r5 = routeRepository.save(new Route(factory1, port, new BigDecimal("2200.00"), 14, 3000));
        Route r6 = routeRepository.save(new Route(factory2, port, new BigDecimal("800.00"), 6, 3500));
        Route r7 = routeRepository.save(new Route(port, warehouse1, new BigDecimal("600.00"), 4, 4000));
        Route r8 = routeRepository.save(new Route(port, warehouse2, new BigDecimal("2400.00"), 16, 4000));
        Route r9 = routeRepository.save(new Route(port, warehouse3, new BigDecimal("3100.00"), 22, 2500));
        routeRepository.save(new Route(factory1, warehouse2, new BigDecimal("1900.00"), 12, 2000));

        // 12 shipments, all on port-touching routes r5-r9, exactly like V3's seed data: 2+2+3+3+2.
        shipmentRepository.saveAll(List.of(
                new Shipment(r5, factory1, port, 420, Instant.now()),
                new Shipment(r5, factory1, port, 380, Instant.now()),
                new Shipment(r6, factory2, port, 510, Instant.now()),
                new Shipment(r6, factory2, port, 275, Instant.now()),
                new Shipment(r7, port, warehouse1, 300, Instant.now()),
                new Shipment(r7, port, warehouse1, 260, Instant.now()),
                new Shipment(r7, port, warehouse1, 190, Instant.now()),
                new Shipment(r8, port, warehouse2, 640, Instant.now()),
                new Shipment(r8, port, warehouse2, 355, Instant.now()),
                new Shipment(r8, port, warehouse2, 480, Instant.now()),
                new Shipment(r9, port, warehouse3, 220, Instant.now()),
                new Shipment(r9, port, warehouse3, 175, Instant.now())));
    }

    @Test
    void disruptingPortAffectsExactlyTwelveShipmentsAndThreeWarehouses() {
        ImpactAssessment impact = riskEngine.assess(reload(port), DisruptionSeverity.CRITICAL);

        assertThat(impact.affectedShipments()).hasSize(12);
        assertThat(impact.affectedWarehouses()).hasSize(3);
    }

    @Test
    void disruptingPortReachesEntireConnectedGraphButShipmentAndWarehouseCountsStillMatchScenario() {
        ImpactAssessment impact = riskEngine.assess(reload(port), DisruptionSeverity.CRITICAL);

        // Every other node in the seed topology is reachable from the port (it's the network's
        // single chokepoint), and every route is touched by the traversal - proving the BFS is a
        // real, unbounded graph traversal, not hardcoded to the port's direct neighbors. Even so,
        // affected shipments/warehouses land on exactly 12/3 because shipments only exist on
        // routes r5-r9 and there are only 3 Warehouse-typed nodes in the whole graph.
        assertThat(impact.affectedNodes()).hasSize(7);
        assertThat(impact.affectedRoutes()).hasSize(10);
        assertThat(impact.affectedShipments()).hasSize(12);
        assertThat(impact.affectedWarehouses()).hasSize(3);
    }

    @Test
    void bfsDoesNotCrossIntoADisconnectedComponent() {
        Warehouse islandWarehouse = networkNodeRepository.save(
                new Warehouse("Unrelated Depot", 25.0, 89.0, NodeStatus.OPERATIONAL, 1000, 200));
        Supplier islandSupplier = networkNodeRepository.save(
                new Supplier("Unrelated Supplier", 25.1, 89.1, NodeStatus.OPERATIONAL, 0.5, 2));
        routeRepository.save(new Route(islandSupplier, islandWarehouse, BigDecimal.TEN, 5, 100));

        ImpactAssessment impact = riskEngine.assess(reload(port), DisruptionSeverity.HIGH);

        assertThat(impact.affectedNodes()).noneMatch(n -> n.getId().equals(islandWarehouse.getId()));
        assertThat(impact.affectedWarehouses()).hasSize(3);
    }

    @Test
    void riskScoreIncreasesWithSeverityForTheSameImpact() {
        int lowRisk = riskEngine.assess(reload(port), DisruptionSeverity.LOW).riskScore();
        int criticalRisk = riskEngine.assess(reload(port), DisruptionSeverity.CRITICAL).riskScore();

        assertThat(criticalRisk).isGreaterThan(lowRisk);
    }

    @Test
    void excludesDeliveredAndCancelledShipmentsFromAffectedCount() {
        List<Shipment> onR7 = shipmentRepository.findAll().stream()
                .filter(s -> s.getRoute().getId().equals(findRouteBetween(port, warehouse1).getId()))
                .toList();
        Shipment toDeliver = onR7.get(0);
        toDeliver.startTransit();
        toDeliver.markDelivered();
        shipmentRepository.save(toDeliver);

        ImpactAssessment impact = riskEngine.assess(reload(port), DisruptionSeverity.HIGH);

        assertThat(impact.affectedShipments()).hasSize(11);
    }

    private NetworkNode reload(NetworkNode node) {
        return networkNodeRepository.findById(node.getId()).orElseThrow();
    }

    private Route findRouteBetween(NetworkNode origin, NetworkNode destination) {
        return routeRepository.findAll().stream()
                .filter(r -> r.getOrigin().getId().equals(origin.getId())
                        && r.getDestination().getId().equals(destination.getId()))
                .findFirst()
                .orElseThrow();
    }
}
