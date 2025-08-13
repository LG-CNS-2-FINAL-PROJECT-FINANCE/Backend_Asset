package com.ddiring.backend_asset.repository;

import com.ddiring.backend_asset.entitiy.Escrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EscrowRepository extends JpaRepository<Escrow, Integer> {
    Optional<Escrow> findByProjectId(String projectId);
}
