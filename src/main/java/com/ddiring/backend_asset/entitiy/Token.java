package com.ddiring.backend_asset.entitiy;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_seq", nullable = false)
    private Integer tokenSeq;

    @Column(name = "wallet_seq", nullable = false)
    private Integer walletSeq;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "created_id")
    private Integer createdId;

    @Column(name = "created_at")
    private Integer createdAt;

    @Column(name = "updated_id")
    private Integer updatedId;

    @Column(name = "updated_at")
    private Integer updatedAt;

    @Builder
    public Token(Integer walletSeq, Integer amount, Integer price, Integer createdId, Integer createdAt, Integer updatedId, Integer updatedAt) {
        this.walletSeq = walletSeq;
        this.amount = amount;
        this.price = price;
        this.createdId = createdId;
        this.createdAt = createdAt;
        this.updatedId = updatedId;
        this.updatedAt = updatedAt;
    }
}
