package com.ddiring.backend_asset.repository;

import com.ddiring.backend_asset.entitiy.EscrowHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscrowHistoryRepository extends JpaRepository<EscrowHistory, Integer> {
    List<EscrowHistory> findByUserSeqAndRollOrderByTransferDateDesc(String userSeq, Integer roll);
    List<EscrowHistory> findByUserSeqAndRollAndTransferTypeOrderByTransferDateDesc(String userSeq, Integer roll, Integer transferType);
}
