package com.ddiring.backend_asset.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssetEscrowRequest {
    private Long tradeId;
    private Long price;
}