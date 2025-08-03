package com.ddiring.backend_asset.repository;

import com.ddiring.backend_asset.entitiy.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Integer> {
    Optional<Bank> findByUserSeq(Integer userSeq);
}
