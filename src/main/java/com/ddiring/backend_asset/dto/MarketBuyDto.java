package com.ddiring.backend_asset.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MarketBuyDto {
    private Integer ordersId;
    private Integer buyPrice;
    private String projectId;
}
