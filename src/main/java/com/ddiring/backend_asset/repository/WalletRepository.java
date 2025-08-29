package com.ddiring.backend_asset.repository;

import com.ddiring.backend_asset.entitiy.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {
    Optional<Wallet> findByUserSeq(String userSeq);
    Optional<Wallet> findByWalletAddress(String walletAddress);
}
