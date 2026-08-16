package com.resilichain.api.repository;

import com.resilichain.api.domain.Disruption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisruptionRepository extends JpaRepository<Disruption, Long> {
}
