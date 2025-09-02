package com.ddiring.backend_asset.api.escrow;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscrowDto {
    private String account;
    private String userSeq;
    private Integer transSeq;
    private Integer transType;
    private Integer amount;
}
