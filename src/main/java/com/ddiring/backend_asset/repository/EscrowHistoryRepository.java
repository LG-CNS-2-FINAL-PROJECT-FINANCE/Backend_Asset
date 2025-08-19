package com.ddiring.backend_asset.repository;

import com.ddiring.backend_asset.entitiy.EscrowHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscrowHistoryRepository extends JpaRepository<EscrowHistory, Integer> {
    List<EscrowHistory> findByUserSeqAndRoleOrderByTransferDateDesc(String userSeq, Integer role);
    List<EscrowHistory> findByUserSeqAndRoleAndTransferTypeOrderByTransferDateDesc(String userSeq, Integer role, Integer transferType);
}
