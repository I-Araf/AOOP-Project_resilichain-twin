package com.resilichain.api.repository;

import com.resilichain.api.domain.Port;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortRepository extends JpaRepository<Port, Long> {
}
