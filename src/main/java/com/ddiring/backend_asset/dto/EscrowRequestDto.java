package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.api.market.MarketDto;
import com.ddiring.backend_asset.api.product.ProductDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EscrowRequestDto {
    private MarketDto marketDto;
    private ProductDto productDto;
}