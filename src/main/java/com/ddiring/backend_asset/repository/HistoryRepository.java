package com.ddiring.backend_asset.repository;

import com.ddiring.backend_asset.entitiy.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Integer> {
    List<History> findByUserSeqAndRollAndMoneyTypeOrderByBankTimeDesc(Integer bankSeq, Integer roll, Integer moneyType);
    List<History> findByUserSeqAndRollOrderByBankTimeDesc(Integer bankSeq, Integer roll);
}
