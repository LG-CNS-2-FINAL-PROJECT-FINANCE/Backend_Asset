// WalletTokenInfoDto.java
package com.ddiring.backend_asset.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class WalletTokenInfoDto {
    private String tokenName;
    private String tokenSymbol;
    private BigDecimal tokenAmount;
    private BigDecimal tokenPriceInKRW;
}