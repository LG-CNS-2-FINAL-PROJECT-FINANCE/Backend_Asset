package com.ddiring.backend_asset.api.market;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketTokenDto {
    private String userSeq;
    private Integer tokenQuantity;
    private Integer perPrice;
}
