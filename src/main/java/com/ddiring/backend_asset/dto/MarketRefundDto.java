package com.ddiring.backend_asset.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketRefundDto {
    private Integer ordersId;
    private String projectId;
    private Integer refundPrice;
    private Integer refundAmount;
    private Integer orderType;
}
