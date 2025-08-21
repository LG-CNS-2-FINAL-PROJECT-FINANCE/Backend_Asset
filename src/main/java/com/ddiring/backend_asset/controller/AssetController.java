package com.ddiring.backend_asset.controller;

import com.ddiring.backend_asset.api.market.MarketDto;
import com.ddiring.backend_asset.api.product.ProductDto;
import com.ddiring.backend_asset.common.dto.ApiResponseDto;
import com.ddiring.backend_asset.common.util.GatewayRequestHeaderUtils;
import com.ddiring.backend_asset.dto.*;
import com.ddiring.backend_asset.entitiy.EscrowHistory;
import com.ddiring.backend_asset.service.BankService;
import com.ddiring.backend_asset.service.WalletService;
import jnr.ffi.annotations.In;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asset")
@RequiredArgsConstructor
public class AssetController {
    private final BankService bankService;
    private final WalletService walletService;

    @PostMapping("/account") //뱅크 생성
    public ApiResponseDto<String> createBank() {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getClientDevice();
        bankService.createBank(userSeq, role);
        return ApiResponseDto.createOk("물주 생성 굿");
    }

    @GetMapping("/account/search")
    public ApiResponseDto<BankSearchDto> bankSearch() {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getClientDevice();
        BankSearchDto history = bankService.bankSearch(userSeq, role);
        return ApiResponseDto.createOk(history);
    }

    @PostMapping("/deposit") //입금
    public ApiResponseDto<Integer> deposit(@RequestHeader("userSeq") String userSeq, @RequestHeader("role") String role, @RequestBody DepositDto depositDto) {
        bankService.deposit(userSeq, role, depositDto);
        return ApiResponseDto.createOk(depositDto.getDeposit());
    }

    @PostMapping("/withdrawal")
    public ApiResponseDto<Integer> withdrawal(@RequestHeader("userSeq") String userSeq, @RequestHeader("role") String role, @RequestBody WithdrawalDto withdrawalDto) {
        bankService.withdrawal(userSeq, role, withdrawalDto);
        return ApiResponseDto.createOk(withdrawalDto.getWithdrawal());
    }

    @PostMapping("/wallet")
    public ApiResponseDto<CreateWalletAddressDto> createWallet(@RequestHeader("userSeq") String userSeq) {
        CreateWalletAddressDto wallet = walletService.createWalletAndReturnKeys(userSeq);
        return ApiResponseDto.createOk(wallet);
    }

    @GetMapping("/{userSeq}/wallet-tokens")
    public ApiResponseDto<List<WalletTokenInfoDto>> getWalletTokens(@RequestHeader("userSeq") String userSeq) {
        List<WalletTokenInfoDto> walletTokenInfoList = walletService.getWalletTokenInfo(userSeq);
        return ApiResponseDto.createOk(walletTokenInfoList);
    }

    @GetMapping("/{userSeq}/history/{moneyType}")
    public ApiResponseDto<List<MoneyMoveDto>> history(@RequestHeader("userSeq") String userSeq, @RequestHeader("role") String role, @PathVariable Integer moneyType, @RequestBody MoneyMoveDto moneyMoveDto) {
        List<MoneyMoveDto> history = bankService.moneyMove(userSeq, role, moneyType);
        return ApiResponseDto.createOk(history);
    }

    @GetMapping("/{userSeq}/history")
    public ApiResponseDto<List<MoneyMoveDto>> allhistory(@RequestHeader("userSeq") String userSeq, @RequestHeader("role") String role) {
        List<MoneyMoveDto> history = bankService.allmoneyMove(userSeq, role);
        return ApiResponseDto.createOk(history);
    }

    @PostMapping("/escrow/account")
    public ApiResponseDto<String> registerEscrow(
            @RequestBody ProductDto productDto) {
        bankService.escrowAccount(productDto);
        return ApiResponseDto.defaultOk();
    }

    @PostMapping("/escrow/deposit")
    public ApiResponseDto<Integer> depositToEscrow(
            @RequestHeader("userSeq") String userSeq,
            @RequestHeader("role") String role,
            @RequestBody EscrowRequestDto escrowRequestDto) {

        Integer money = bankService.depositToEscrow(escrowRequestDto.getMarketDto(), escrowRequestDto.getProductDto(),  role, userSeq);
        return ApiResponseDto.createOk(money);
    }

    @PostMapping("/escrow/withdrawal")
    public ApiResponseDto<Integer> withdrawalFromEscrow(
            @RequestHeader("userSeq") String userSeq,
            @RequestHeader("role") String role,
            @RequestBody EscrowRequestDto escrowRequestDto) {

        Integer money = bankService.withdrawalFromEscrow(escrowRequestDto.getMarketDto(), escrowRequestDto.getProductDto(), role, userSeq);
        return ApiResponseDto.createOk(money);
    }


}