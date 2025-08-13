package com.ddiring.backend_asset.api.market;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketDto {
    private Integer userSeq;
    private Integer price;
}
