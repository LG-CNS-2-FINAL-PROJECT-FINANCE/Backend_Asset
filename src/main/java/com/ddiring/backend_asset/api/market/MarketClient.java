package com.ddiring.backend_asset.api.market;

import com.ddiring.backend_asset.api.product.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

// TODO: 병합 시 주소 맞춰야함
@FeignClient(name = "market")
public interface MarketClient {

    @GetMapping("/api/asset/deposit")
    ProductDto getProduct(@RequestParam("userSeq") String userSeq, @RequestParam("role") String role);
}
