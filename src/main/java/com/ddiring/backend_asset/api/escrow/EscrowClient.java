package com.ddiring.backend_asset.api.escrow;

import com.ddiring.backend_asset.common.dto.ApiResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "escrowClient", url = "${escrow.base-url}")
public interface EscrowClient {
    @PostMapping("/api/escrow/deposit")
    ApiResponseDto<EscrowDto> escrowDeposit(@RequestBody EscrowDto escrowDto);

}
