package com.ddiring.backend_asset.api.product;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DistributionDto {
    private String userSeq;
    private String ProjectId;
    private Integer distributionAmount;
}
