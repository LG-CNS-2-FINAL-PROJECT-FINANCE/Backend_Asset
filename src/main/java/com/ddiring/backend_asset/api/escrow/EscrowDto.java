package com.ddiring.backend_asset.api.escrow;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
public class EscrowDto {
    private String account;
    private String userSeq;
    private Integer transSeq;
    private Integer transType;
    private BigDecimal amount;
}
