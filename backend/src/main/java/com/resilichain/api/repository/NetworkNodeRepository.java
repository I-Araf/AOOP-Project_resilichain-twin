package com.resilichain.api.repository;

import com.resilichain.api.domain.NetworkNode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NetworkNodeRepository extends JpaRepository<NetworkNode, Long> {
}
