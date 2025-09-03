package com.ddiring.backend_asset.repository;

import com.ddiring.backend_asset.entitiy.Token;
import com.ddiring.backend_asset.entitiy.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {
    List<Token> findByUserSeq(String userSeq);
    Optional<Token> findByUserSeqAndProjectId(String userSeq, String projectId);
    List<Token> findByProjectId(String projectId);
}
