package com.ddiring.backend_asset.entitiy;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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
    private Integer userSeq;

    @Column(name = "escrow_account", nullable = false)
    private String escrowAccount;

    @Column(name = "title")
    private String title;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "transfer_type", nullable = false)
    private Integer transferType;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Builder
    public EscrowHistory(Integer userSeq, String escrowAccount, String title, Integer price, Integer transferType, LocalDate transferDate) {
        this.userSeq = userSeq;
        this.escrowAccount = escrowAccount;
        this.title = title;
        this.price = price;
        this.transferType = transferType;
        this.transferDate = transferDate;
    }
}
