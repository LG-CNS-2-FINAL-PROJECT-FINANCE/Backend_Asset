package com.ddiring.backend_asset.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MarketRefundDto {
    private Integer ordersId;
    private Integer refundPrice;
    private String projectId;
}
