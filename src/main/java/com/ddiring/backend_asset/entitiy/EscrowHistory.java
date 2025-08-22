package com.ddiring.backend_asset.entitiy;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "escrow_history")
public class EscrowHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "escrow_history_seq")
    private Integer escrowHistorySeq;

    @Column(name = "user_seq", nullable = false)
    private String userSeq;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "escrow_account", nullable = false)
    private String escrowAccount;

    @Column(name = "title")
    private String title;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "transfer_type", nullable = false)
    private Integer transferType;

    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;

    @Builder
    public EscrowHistory(String userSeq, String role, String escrowAccount, String title, Long price, Integer transferType, LocalDateTime transferDate) {
        this.userSeq = userSeq;
        this.escrowAccount = escrowAccount;
        this.role = role;
        this.title = title;
        this.price = price;
        this.transferType = transferType;
        this.transferDate = transferDate;
    }
}
