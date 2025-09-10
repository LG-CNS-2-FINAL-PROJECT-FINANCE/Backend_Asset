package com.ddiring.backend_asset.entitiy;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate; // java.time.LocalDate import 필요
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "bank")
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_seq", nullable = false)
    private Integer bankSeq;

    @Column(name = "user_seq", nullable = false)
    private String userSeq;

    @Column(name = "bank_number", nullable = false)
    private String bankNumber;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "distribution")
    private Integer distribution;

    @Column(name = "deposit", nullable = false)
    private Integer deposit;

    @Column(name = "linked_at")
    private LocalDateTime linkedAt;

    @Column(name = "created_id")
    private Integer createdId;

    @Column(name = "created_at")
    private LocalDate createdAt; // Integer -> LocalDate로 수정

    @Column(name = "updated_id")
    private Integer updatedId;

    @Column(name = "updated_at")
    private LocalDate updatedAt; // Integer -> LocalDate로 수정

    @Builder
    public Bank(String userSeq, Integer deposit, Integer distribution, String bankNumber, String role, LocalDateTime linkedAt, Integer createdId, LocalDate createdAt, Integer updatedId, LocalDate updatedAt) {
        this.userSeq = userSeq;
        this.bankNumber = bankNumber;
        this.distribution = distribution;
        this.role = role;
        this.deposit = deposit;
        this.linkedAt = linkedAt;
        this.createdId = createdId;
        this.createdAt = createdAt;
        this.updatedId = updatedId;
        this.updatedAt = updatedAt;
    }
}