package com.ddiring.backend_asset.api.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
public class ProductDto {
    private String projectId;
    private String account;
    public ProductDto(String projectId, String account) {
        this.projectId = projectId;
        this.account = account;
    }
}
