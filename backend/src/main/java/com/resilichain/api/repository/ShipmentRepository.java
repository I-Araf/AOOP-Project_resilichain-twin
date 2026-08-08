package com.resilichain.api.repository;

import com.resilichain.api.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}
