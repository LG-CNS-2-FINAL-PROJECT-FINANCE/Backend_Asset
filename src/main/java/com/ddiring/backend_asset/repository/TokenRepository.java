package com.ddiring.backend_asset.repository;

import com.ddiring.backend_asset.entitiy.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {
}
