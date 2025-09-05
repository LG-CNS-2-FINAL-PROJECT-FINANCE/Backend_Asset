package com.ddiring.backend_asset.entitiy;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_seq", nullable = false)
    private Integer walletSeq;

    @Column(name = "user_seq", nullable = false, unique = true)
    private String userSeq;

    @Column(name = "wallet_address", nullable = false)
    private String walletAddress;

    @Column(name = "created_id")
    private Integer createdId;

    @Lob
    @Column(name = "private_key")
    private byte[] privateKey;

    @Column(name = "created_at")
    private Integer createdAt;

    @Column(name = "updated_id")
    private Integer updatedId;

    @Column(name = "updated_at")
    private Integer updatedAt;

    @Builder
    public Wallet(String userSeq, String walletAddress, byte[] privateKey, Integer createdId, Integer createdAt, Integer updatedId, Integer updatedAt) {
        this.userSeq = userSeq;
        this.walletAddress = walletAddress;
        this.privateKey = privateKey;
        this.createdId = createdId;
        this.createdAt = createdAt;
        this.updatedId = updatedId;
        this.updatedAt = updatedAt;
    }
}
