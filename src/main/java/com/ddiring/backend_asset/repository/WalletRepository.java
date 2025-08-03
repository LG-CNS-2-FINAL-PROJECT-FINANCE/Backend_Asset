package com.ddiring.backend_asset.repository;

import com.ddiring.backend_asset.entitiy.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {
}
