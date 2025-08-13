package com.ddiring.backend_asset.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PayDto {
    private Integer userSeq;
    private Integer price;

    public PayDto(Integer userSeq, Integer price) {
        this.userSeq = userSeq;
        this.price = price;
    }
}
