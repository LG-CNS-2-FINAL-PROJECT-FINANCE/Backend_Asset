// WalletTokenInfoDto.java
package com.ddiring.backend_asset.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class WalletTokenInfoDto {
    private String projectId;
    private String userSeq;
    private Integer amount;
    private Integer price;
}