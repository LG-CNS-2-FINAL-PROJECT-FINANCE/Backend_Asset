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
@Table(name = "escrow")
public class Escrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "escrow_seq", nullable = false)
    private Integer escrowSeq;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "account")
    private String account;

    @Builder
    public Escrow(String account, String projectId) {
        this.account = account;
        this.projectId = projectId;
    }
}
