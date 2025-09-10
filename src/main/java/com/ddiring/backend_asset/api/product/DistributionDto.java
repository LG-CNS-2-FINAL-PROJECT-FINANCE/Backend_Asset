package com.ddiring.backend_asset.api.product;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DistributionDto {
    private String projectId;
    private Integer distributionAmount;
    private String userSeq;
}
