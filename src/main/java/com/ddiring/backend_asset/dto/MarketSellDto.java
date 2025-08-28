package com.ddiring.backend_asset.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MarketSellDto {
    public Integer transType;
    private Integer sellToken;
    private Integer ordersId;
//    private String tokenSymbol;
    private String projectId;
}
