package com.ddiring.backend_asset.entitiy;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

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
    private String userSeq;

    @Column(name = "role") //0 창작자 1 투자자
    private String role;

    @Column(name = "bank_number")
    private String bankNumber;

    @Column(name = "bank_price")
    private Long bankPrice;

    @Column(name = "bank_time")
    private LocalDate bankTime;

    @Column(name = "money_type") //0 입금 1 출금
    private Integer moneyType;

    @CreatedBy
    @Column(name = "created_id")
    private Integer createdId;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDate createdAt;

    @LastModifiedBy
    @Column(name = "updated_id")
    private Integer updatedId;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Builder
    public History(String userSeq, String role, String bankNumber, Long bankPrice, LocalDate bankTime,Integer moneyType, Integer createdId, LocalDate createdAt, Integer updatedId, LocalDate updatedAt) {
        this.userSeq = userSeq;
        this.role = role;
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
