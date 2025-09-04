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
    private String title;
    private Integer amount;
    private Integer price;
    private Integer currentPrice;
    public WalletTokenInfoDto(Token token) {
        this.title = token.getTitle();
        this.amount = token.getAmount();
        this.price = token.getPrice();
        this.currentPrice = token.getCurrentPrice();
    }
}
