package com.ddiring.backend_asset.api.escrow;

import com.ddiring.backend_asset.common.dto.ApiResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "escrowClient", url = "http://localhost:8084")
public interface EscrowClient {

    @PostMapping("/api/escrow/deposit")
    ResponseEntity<String> escrowDeposit(@RequestBody EscrowDto escrowDto);

    @PostMapping("/api/escrow/withdrawal")
    ResponseEntity<String> escrowWithdrawal(@RequestBody EscrowDto escrowDto);

    @PostMapping("/api/escrow/refund")
    ResponseEntity<String> escrowRefund(@RequestBody EscrowDto escrowDto);
}
