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

    @Column(name = "title")
    private String title;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "account")
    private String account;

    @Column(name = "distribution")
    private Integer distribution;

    @Builder
    public Escrow(String title, String account, String projectId, Integer distribution) {
        this.title = title;
        this.account = account;
        this.projectId = projectId;
        this.distribution = distribution;
    }
}
