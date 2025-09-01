package com.ddiring.backend_asset.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketRefundDto {
    private Integer ordersId;
    private Integer refundPrice;
    private String projectId;
}
