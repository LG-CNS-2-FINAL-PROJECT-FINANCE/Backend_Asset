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
@Table(name = "history")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id", nullable = false)
    private Integer historyId;

    @Column(name = "user_seq")
    private Integer userSeq;

    @Column(name = "bank_type")
    private Integer bankType;

    @Column(name = "bank_number")
    private String bankNumber;

    @Column(name = "bank_price")
    private Integer bankPrice;

    @Column(name = "bank_time")
    private LocalDate bankTime;

    @Column(name = "money_type")
    private Integer moneyType;

    @Column(name = "created_id")
    private Integer createdId;

    @Column(name = "created_at")
    private Integer createdAt;

    @Column(name = "updated_id")
    private Integer updatedId;

    @Column(name = "updated_at")
    private Integer updatedAt;

    @Builder
    public History(Integer userSeq, Integer bankType, String bankNumber, Integer bankPrice, LocalDate bankTime,Integer moneyType, Integer createdId, Integer createdAt, Integer updatedId, Integer updatedAt) {
        this.userSeq = userSeq;
        this.bankType = bankType;
        this.bankNumber = bankNumber;
        this.bankPrice = bankPrice;
        this.bankTime = bankTime;
        this.moneyType = moneyType;
        this.createdId = createdId;
        this.createdAt = createdAt;
        this.updatedId = updatedId;
        this.updatedAt = updatedAt;
    }
}
