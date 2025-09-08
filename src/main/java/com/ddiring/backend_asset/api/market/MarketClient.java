package com.ddiring.backend_asset.api.market;

import com.ddiring.backend_asset.common.dto.ApiResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "marketClient", url = "${market.base-url}") // Eureka 등에 등록된 Market 서비스 이름
public interface MarketClient {

    @GetMapping("/api/market/trade/{tradeId}")
    ApiResponseDto<TradeInfoResponseDto> getTradeInfo(@PathVariable("tradeId") Long tradeId);
}