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
    private String title;
    private String account;
    public  ProductDto(String projectId, String title, String account) {
        this.projectId = projectId;
        this.title = title;
        this.account = account;
    }
}
