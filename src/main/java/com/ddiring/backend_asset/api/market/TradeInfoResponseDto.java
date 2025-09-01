package com.ddiring.backend_asset.api.market;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TradeInfoResponseDto {
    private Long tradeId;
    private String projectId;
    private Integer price;
    private Integer tokenQuantity;
    private String buyerUserSeq;
    private String sellerUserSeq;
}