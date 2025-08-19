package com.ddiring.backend_asset.entitiy;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    private Integer role;

    @Column(name = "deposit", nullable = false)
    private Integer deposit;

    @Column(name = "linked_at", nullable = false)
    private LocalDate linkedAt;

    @Column(name = "created_id")
    private Integer createdId;

    @Column(name = "created_at")
    private Integer createdAt;

    @Column(name = "updated_id")
    private Integer updatedId;

    @Column(name = "updated_at")
    private Integer updatedAt;

    @Builder
    public Bank(String userSeq, Integer deposit, String bankNumber, Integer role, LocalDate linkedAt, Integer createdId, Integer createdAt, Integer updatedId, Integer updatedAt) {
        this.userSeq = userSeq;
        this.bankNumber = bankNumber;
        this.role = role;
        this.deposit = deposit;
        this.linkedAt = linkedAt;
        this.createdId = createdId;
        this.createdAt = createdAt;
        this.updatedId = updatedId;
        this.updatedAt = updatedAt;
    }
}
