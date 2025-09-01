package com.ddiring.backend_asset.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSellerAssetDto {
    private Long tradeId;
    private String projectId;
    private String buyAddress;
    private Long buyTokenAmount;
    private String sellAddress;
    private Long sellTokenAmount; // 판매자에게 입금될 최종 금액
}