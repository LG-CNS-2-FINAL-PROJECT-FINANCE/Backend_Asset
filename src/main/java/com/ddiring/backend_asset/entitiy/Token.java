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

    @Column(name = "contract_address", nullable = false)
    private String contractAddress;

    @Column(name = "token_name", nullable = false)
    private String tokenName;

    @Column(name = "token_symbol", nullable = false)
    private String tokenSymbol;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "created_id")
    private Integer createdId;

    @Column(name = "created_at")
    private Integer createdAt;

    @Column(name = "updated_id")
    private Integer updatedId;

    @Column(name = "updated_at")
    private Integer updatedAt;

    @Builder
    public Token(Integer walletSeq, String contractAddress, String tokenName, String tokenSymbol, Integer amount, Long price, Integer createdId, Integer createdAt, Integer updatedId, Integer updatedAt) {
        this.walletSeq = walletSeq;
        this.contractAddress = contractAddress;
        this.tokenName = tokenName;
        this.tokenSymbol = tokenSymbol;
        this.amount = amount;
        this.price = price;
        this.createdId = createdId;
        this.createdAt = createdAt;
        this.updatedId = updatedId;
        this.updatedAt = updatedAt;
    }
}
