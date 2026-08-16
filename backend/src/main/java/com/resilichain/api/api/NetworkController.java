package com.resilichain.api.api;

import com.resilichain.api.api.dto.FactoryRequest;
import com.resilichain.api.api.dto.FactoryResponse;
import com.resilichain.api.api.dto.NetworkGraphResponse;
import com.resilichain.api.api.dto.PortRequest;
import com.resilichain.api.api.dto.PortResponse;
import com.resilichain.api.api.dto.RouteRequest;
import com.resilichain.api.api.dto.RouteResponse;
import com.resilichain.api.api.dto.SupplierRequest;
import com.resilichain.api.api.dto.SupplierResponse;
import com.resilichain.api.api.dto.WarehouseRequest;
import com.resilichain.api.api.dto.WarehouseResponse;
import com.resilichain.api.domain.Factory;
import com.resilichain.api.domain.NetworkNode;
import com.resilichain.api.domain.Port;
import com.resilichain.api.domain.Route;
import com.resilichain.api.domain.Supplier;
import com.resilichain.api.domain.Warehouse;
import com.resilichain.api.repository.FactoryRepository;
import com.resilichain.api.repository.NetworkNodeRepository;
import com.resilichain.api.repository.PortRepository;
import com.resilichain.api.repository.RouteRepository;
import com.resilichain.api.repository.SupplierRepository;
import com.resilichain.api.repository.WarehouseRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/network")
public class NetworkController {

    private final SupplierRepository supplierRepository;
    private final FactoryRepository factoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final PortRepository portRepository;
    private final RouteRepository routeRepository;
    private final NetworkNodeRepository networkNodeRepository;

    public NetworkController(SupplierRepository supplierRepository, FactoryRepository factoryRepository,
                              WarehouseRepository warehouseRepository, PortRepository portRepository,
                              RouteRepository routeRepository, NetworkNodeRepository networkNodeRepository) {
        this.supplierRepository = supplierRepository;
        this.factoryRepository = factoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.portRepository = portRepository;
        this.routeRepository = routeRepository;
        this.networkNodeRepository = networkNodeRepository;
    }

    @GetMapping("/suppliers")
    public List<SupplierResponse> listSuppliers() {
        return supplierRepository.findAll().stream().map(SupplierResponse::from).toList();
    }

    @PostMapping("/suppliers")
    public SupplierResponse createSupplier(@Valid @RequestBody SupplierRequest request) {
        Supplier supplier = new Supplier(request.name(), request.latitude(), request.longitude(),
                request.status(), request.reliabilityRating(), request.leadTimeCapabilityDays());
        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    @GetMapping("/factories")
    public List<FactoryResponse> listFactories() {
        return factoryRepository.findAll().stream().map(FactoryResponse::from).toList();
    }

    @PostMapping("/factories")
    public FactoryResponse createFactory(@Valid @RequestBody FactoryRequest request) {
        Factory factory = new Factory(request.name(), request.latitude(), request.longitude(),
                request.status(), request.productionCapacity(), request.currentOutput());
        return FactoryResponse.from(factoryRepository.save(factory));
    }

    @GetMapping("/warehouses")
    public List<WarehouseResponse> listWarehouses() {
        return warehouseRepository.findAll().stream().map(WarehouseResponse::from).toList();
    }

    @PostMapping("/warehouses")
    public WarehouseResponse createWarehouse(@Valid @RequestBody WarehouseRequest request) {
        Warehouse warehouse = new Warehouse(request.name(), request.latitude(), request.longitude(),
                request.status(), request.capacity(), request.currentStock());
        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    @GetMapping("/ports")
    public List<PortResponse> listPorts() {
        return portRepository.findAll().stream().map(PortResponse::from).toList();
    }

    @PostMapping("/ports")
    public PortResponse createPort(@Valid @RequestBody PortRequest request) {
        Port port = new Port(request.name(), request.latitude(), request.longitude(),
                request.status(), request.throughputCapacity(), request.operationalStatus());
        return PortResponse.from(portRepository.save(port));
    }

    @GetMapping("/graph")
    public NetworkGraphResponse networkGraph() {
        return NetworkGraphResponse.from(networkNodeRepository.findAll(), routeRepository.findAll());
    }

    @GetMapping("/routes")
    public List<RouteResponse> listRoutes() {
        return routeRepository.findAll().stream().map(RouteResponse::from).toList();
    }

    @PostMapping("/routes")
    public RouteResponse createRoute(@Valid @RequestBody RouteRequest request) {
        NetworkNode origin = findNode(request.originId());
        NetworkNode destination = findNode(request.destinationId());
        Route route = new Route(origin, destination, request.cost(), request.leadTimeHours(), request.capacity());
        return RouteResponse.from(routeRepository.save(route));
    }

    private NetworkNode findNode(Long id) {
        return networkNodeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No network node with id " + id));
    }
}
