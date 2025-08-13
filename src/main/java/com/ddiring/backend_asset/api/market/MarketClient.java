package com.ddiring.backend_asset.api.market;

import com.ddiring.backend_asset.api.product.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// TODO: 병합 시 주소 맞춰야함
@FeignClient(name = "market")
public interface MarketClient {

    @GetMapping("/api/product/{productId}")
    ProductDto getProduct(@PathVariable("productID") String productId);
}
