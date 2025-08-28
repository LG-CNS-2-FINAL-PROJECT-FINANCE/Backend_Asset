// WalletTokenInfoDto.java
package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.entitiy.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class WalletTokenInfoDto {
    private String projectId;
    private String tokenSymbol;
    private Integer amount;
    private Integer price;
    public WalletTokenInfoDto(Token token) {
        this.projectId = token.getProjectId();
        this.tokenSymbol = token.getTokenSymbol();
        this.amount = token.getAmount();
        this.price = token.getPrice();
    }
}


//김민우 바보 O.<