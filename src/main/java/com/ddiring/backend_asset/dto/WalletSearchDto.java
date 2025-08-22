package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.entitiy.Token;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WalletSearchDto {
    private Integer tokenSeq;
    private Integer amount;
    private Long price;

    public WalletSearchDto(Token token) {
        this.tokenSeq = token.getTokenSeq();
        this.amount = token.getAmount();
        this.price = token.getPrice();
    }
}
