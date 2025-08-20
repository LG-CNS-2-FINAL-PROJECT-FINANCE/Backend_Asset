package com.ddiring.backend_asset.repository;

import com.ddiring.backend_asset.entitiy.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Integer> {
    List<History> findByUserSeqAndRoleAndMoneyTypeOrderByBankTimeDesc(String userSeq, String role, Integer moneyType);
    List<History> findByUserSeqAndRoleOrderByBankTimeDesc(String userSeq, String role);
}
