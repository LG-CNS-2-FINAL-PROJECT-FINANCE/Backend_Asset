package com.ddiring.backend_asset.controller;

import com.ddiring.backend_asset.api.product.ProductDto;
import com.ddiring.backend_asset.common.dto.ApiResponseDto;
import com.ddiring.backend_asset.common.util.GatewayRequestHeaderUtils;
import com.ddiring.backend_asset.dto.*;
import com.ddiring.backend_asset.service.BankService;
import com.ddiring.backend_asset.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
        String role = GatewayRequestHeaderUtils.getRole();
        bankService.createBank(userSeq, role);
        return ApiResponseDto.createOk("물주 생성 굿");
    }

    @GetMapping("/account/search")
    public ApiResponseDto<BankSearchDto> bankSearch() {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
        BankSearchDto history = bankService.bankSearch(userSeq, role);
        return ApiResponseDto.createOk(history);
    }

    @PostMapping("/account/deposit") //입금
    public ApiResponseDto<Long> deposit(@RequestBody DepositDto depositDto) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
        bankService.deposit(userSeq, role, depositDto);
        return ApiResponseDto.createOk(depositDto.getDeposit());
    }

    @PostMapping("/account/withdrawal")
    public ApiResponseDto<Long> withdrawal(@RequestBody WithdrawalDto withdrawalDto) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
        bankService.withdrawal(userSeq, role, withdrawalDto);
        return ApiResponseDto.createOk(withdrawalDto.getWithdrawal());
    }

    @PostMapping("/wallet")
    public ApiResponseDto<CreateWalletAddressDto> createWallet() {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        CreateWalletAddressDto wallet = walletService.createWalletAndReturnKeys(userSeq);
        return ApiResponseDto.createOk(wallet);
    }

    @GetMapping("/wallet/search")
    public ApiResponseDto<String> getWalletTokens() {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String walletTokenInfoList = walletService.getWalletAddress(userSeq);
        return ApiResponseDto.createOk(walletTokenInfoList);
    }

    @GetMapping("/{userSeq}/history/{moneyType}")
    public ApiResponseDto<List<MoneyMoveDto>> history(@PathVariable Integer moneyType, @RequestBody MoneyMoveDto moneyMoveDto) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
        List<MoneyMoveDto> history = bankService.moneyMove(userSeq, role, moneyType);
        return ApiResponseDto.createOk(history);
    }

    @GetMapping("/{userSeq}/history")
    public ApiResponseDto<List<MoneyMoveDto>> allhistory() {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
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
    public ApiResponseDto<Long> depositToEscrow(
            @RequestBody EscrowRequestDto escrowRequestDto) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
        Long money = bankService.depositToEscrow(escrowRequestDto.getMarketDto(), escrowRequestDto.getProductDto(),  role, userSeq);
        return ApiResponseDto.createOk(money);
    }

    @PostMapping("/escrow/withdrawal")
    public ApiResponseDto<Long> withdrawalFromEscrow(
            @RequestBody EscrowRequestDto escrowRequestDto) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
        Long money = bankService.withdrawalFromEscrow(escrowRequestDto.getMarketDto(), escrowRequestDto.getProductDto(), role, userSeq);
        return ApiResponseDto.createOk(money);
    }

    @GetMapping("/bank/balance")
    public ApiResponseDto<BankSearchDto> getBankBalance(@RequestParam("userSeq") String userSeq, @RequestParam("role") String role) {
        BankSearchDto bankSearchDto = bankService.bankSearch(userSeq, role);
        return ApiResponseDto.createOk(bankSearchDto);
    }
}