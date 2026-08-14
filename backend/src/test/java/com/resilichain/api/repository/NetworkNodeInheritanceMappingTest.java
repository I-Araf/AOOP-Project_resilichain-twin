package com.resilichain.api.repository;

import com.resilichain.api.domain.Factory;
import com.resilichain.api.domain.NetworkNode;
import com.resilichain.api.domain.NodeStatus;
import com.resilichain.api.domain.Port;
import com.resilichain.api.domain.PortOperationalStatus;
import com.resilichain.api.domain.Route;
import com.resilichain.api.domain.Shipment;
import com.resilichain.api.domain.ShipmentStatus;
import com.resilichain.api.domain.Supplier;
import com.resilichain.api.domain.Warehouse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class NetworkNodeInheritanceMappingTest {

    @Autowired
    private NetworkNodeRepository networkNodeRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Test
    void persistsAndLoadsAllFourSubtypesPolymorphically() {
        Supplier supplier = new Supplier("Acme Textiles", 22.3, 91.8, NodeStatus.OPERATIONAL, 0.9, 3);
        Factory factory = new Factory("Dhaka Plant", 23.8, 90.4, NodeStatus.OPERATIONAL, 1000, 400);
        Warehouse warehouse = new Warehouse("Central WH", 23.7, 90.3, NodeStatus.OPERATIONAL, 5000, 3200);
        Port port = new Port("Chattogram Port", 22.3, 91.8, NodeStatus.OPERATIONAL, 20000, PortOperationalStatus.OPEN);

        networkNodeRepository.saveAll(List.of(supplier, factory, warehouse, port));

        List<NetworkNode> reloaded = networkNodeRepository.findAll();

        assertThat(reloaded).hasSize(4);
        assertThat(reloaded).anyMatch(Supplier.class::isInstance);
        assertThat(reloaded).anyMatch(Factory.class::isInstance);
        assertThat(reloaded).anyMatch(Warehouse.class::isInstance);
        assertThat(reloaded).anyMatch(Port.class::isInstance);

        Warehouse reloadedWarehouse = reloaded.stream()
                .filter(Warehouse.class::isInstance)
                .map(Warehouse.class::cast)
                .findFirst()
                .orElseThrow();
        assertThat(reloadedWarehouse.getCurrentStock()).isEqualTo(3200);
    }

    @Test
    void routeLinksTwoDifferentNodeSubtypesAndReloadsCorrectly() {
        Warehouse warehouse = new Warehouse("Central WH", 23.7, 90.3, NodeStatus.OPERATIONAL, 5000, 3200);
        Port port = new Port("Chattogram Port", 22.3, 91.8, NodeStatus.OPERATIONAL, 20000, PortOperationalStatus.OPEN);
        networkNodeRepository.saveAll(List.of(warehouse, port));

        Route route = new Route(warehouse, port, new BigDecimal("250.00"), 18, 800);
        Long routeId = routeRepository.save(route).getId();

        Route reloadedRoute = routeRepository.findById(routeId).orElseThrow();

        assertThat(reloadedRoute.getOrigin()).isInstanceOf(Warehouse.class);
        assertThat(reloadedRoute.getDestination()).isInstanceOf(Port.class);
        assertThat(reloadedRoute.getCost()).isEqualByComparingTo("250.00");
    }

    @Test
    void shipmentStatusEnumPersistsAsString() {
        Warehouse warehouse = new Warehouse("Central WH", 23.7, 90.3, NodeStatus.OPERATIONAL, 5000, 3200);
        Port port = new Port("Chattogram Port", 22.3, 91.8, NodeStatus.OPERATIONAL, 20000, PortOperationalStatus.OPEN);
        networkNodeRepository.saveAll(List.of(warehouse, port));
        Route route = routeRepository.save(new Route(warehouse, port, new BigDecimal("250.00"), 18, 800));

        Shipment shipment = new Shipment(route, warehouse, port, 120, Instant.now());
        shipment.startTransit();
        shipment.markDelayed();
        Long shipmentId = shipmentRepository.save(shipment).getId();

        Shipment reloaded = shipmentRepository.findById(shipmentId).orElseThrow();

        assertThat(reloaded.getStatus()).isEqualTo(ShipmentStatus.DELAYED);
    }
}
