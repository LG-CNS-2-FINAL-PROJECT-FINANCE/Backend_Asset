package com.ddiring.backend_asset.controller;

import com.ddiring.backend_asset.common.dto.ApiResponseDto;
import com.ddiring.backend_asset.dto.*;
import com.ddiring.backend_asset.service.BankService;
import com.ddiring.backend_asset.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asset")
@RequiredArgsConstructor
public class AssetController {
    private final BankService bankService;
    private final WalletService walletService;

    @PostMapping("/account")
    public ApiResponseDto<String> createBank(@RequestBody CreateBankDto createBankDto) {
        bankService.createBank(createBankDto);
        return ApiResponseDto.defaultOk();
    }

    @PostMapping("/{userId}/account")
    public ApiResponseDto<BankSearchDto> bankSearch(@PathVariable Integer userId, @RequestBody BankSearchDto bankSearchDto) {
        BankSearchDto history = bankService.bankSearch(userId, bankSearchDto.getBankType());
        return ApiResponseDto.createOk(history);
    }

    @PostMapping("/deposit")
    public ApiResponseDto<String> deposit(@RequestBody DepositDto depositDto) {
        bankService.deposit(depositDto);
        return ApiResponseDto.defaultOk();
    }

    @PostMapping("/withdrawal")
    public ApiResponseDto<String> withdrawal(@RequestBody WithdrawalDto withdrawalDto) {
        bankService.withdrawal(withdrawalDto);
        return ApiResponseDto.defaultOk();
    }

    @PostMapping("/wallet")
    public ApiResponseDto<CreateWalletAddressDto> createWallet(@RequestBody CreateWalletDto createWalletDto) {
        CreateWalletAddressDto wallet = walletService.createWalletAndReturnKeys(createWalletDto);
        return ApiResponseDto.createOk(wallet);
    }

    @GetMapping("/{userId}/wallet-tokens") // 새로운 엔드포인트 정의
    public ApiResponseDto<List<WalletTokenInfoDto>> getWalletTokens(@PathVariable Integer userId) {
        List<WalletTokenInfoDto> walletTokenInfoList = walletService.getWalletTokenInfo(userId);
        return ApiResponseDto.createOk(walletTokenInfoList);
    }

    @PostMapping("/{userId}/history")
    public ApiResponseDto<List<MoneyMoveDto>> history(@PathVariable Integer userId, @RequestBody MoneyMoveDto moneyMoveDto) {
        List<MoneyMoveDto> history = bankService.moneyMove(userId, moneyMoveDto.getBankType(), moneyMoveDto.getMoneyType());
        return ApiResponseDto.createOk(history);
    }

    @PostMapping("/{userId}/allhistory")
    public ApiResponseDto<List<MoneyMoveDto>> allhistory(@PathVariable Integer userId, @RequestBody MoneyMoveDto moneyMoveDto) {
        List<MoneyMoveDto> history = bankService.allmoneyMove(userId, moneyMoveDto.getBankType());
        return ApiResponseDto.createOk(history);
    }
    @GetMapping("/info")
    public String getAssetInfo() {
    return "짱짱맨";
    }
}