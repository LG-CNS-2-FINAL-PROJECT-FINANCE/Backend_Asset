package com.ddiring.backend_asset.api.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// TODO: 병합 시 주소 맞춰야함
@FeignClient(name = "product")
public interface ProductClient {

    @GetMapping("/api/product/{productId}")
    ProductDto getProduct(@PathVariable("productId") String productId);
}
