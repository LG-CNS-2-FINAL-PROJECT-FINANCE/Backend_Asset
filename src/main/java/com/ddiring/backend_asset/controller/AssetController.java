package com.ddiring.backend_asset.controller;

import com.ddiring.backend_asset.api.market.MarketDto;
import com.ddiring.backend_asset.api.product.ProductDto;
import com.ddiring.backend_asset.common.dto.ApiResponseDto;
import com.ddiring.backend_asset.common.util.GatewayRequestHeaderUtils;
import com.ddiring.backend_asset.dto.*;
import com.ddiring.backend_asset.service.BankService;
import com.ddiring.backend_asset.service.TokenService;
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
    private final TokenService tokenService;

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
    public ApiResponseDto<Integer> deposit(@RequestBody DepositDto depositDto) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
        bankService.deposit(userSeq, role, depositDto);
        return ApiResponseDto.createOk(depositDto.getPrice());
    }

    @PostMapping("/account/withdrawal")
    public ApiResponseDto<Integer> withdrawal(@RequestBody WithdrawalDto withdrawalDto) {
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
    public ApiResponseDto<String> getWalletAddress() {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String walletTokenInfoList = walletService.getWalletAddress(userSeq);
        return ApiResponseDto.createOk(walletTokenInfoList);
    }


    @GetMapping("/history/{moneyType}")
    public ApiResponseDto<List<MoneyMoveDto>> history(@PathVariable Integer moneyType, @RequestBody MoneyMoveDto moneyMoveDto) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
        List<MoneyMoveDto> history = bankService.moneyMove(userSeq, role, moneyType);
        return ApiResponseDto.createOk(history);
    }

    @GetMapping("/history")
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
    public ApiResponseDto<Integer> depositToEscrow(
            @RequestBody MarketDto marketDto) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
        Integer money = bankService.depositToEscrow(userSeq, role, marketDto);
        return ApiResponseDto.createOk(money);
    }

    @PostMapping("/escrow/withdrawal")
    public ApiResponseDto<Integer> withdrawalFromEscrow(
            @RequestBody MarketDto marketDto) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
        Integer money = bankService.withdrawalFromEscrow(userSeq, role, marketDto);
        return ApiResponseDto.createOk(money);
    }

    @GetMapping("/bank/balance")
    public ApiResponseDto<BankSearchDto> getBankBalance(@RequestParam("userSeq") String userSeq, @RequestParam("role") String role) {
        BankSearchDto bankSearchDto = bankService.bankSearch(userSeq, role);
        return ApiResponseDto.createOk(bankSearchDto);
    }

    @PostMapping("/market/buy")
    public ApiResponseDto<String> marketBuy(@RequestBody MarketBuyDto marketBuyDto) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
        bankService.setBuyPrice(userSeq, role, marketBuyDto);
        return ApiResponseDto.createOk("success");
    }

    @PostMapping("/market/sell")
    public ApiResponseDto<String> marketSell(@RequestBody MarketSellDto marketSellDto) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        tokenService.setSellToken(userSeq, marketSellDto);
        return ApiResponseDto.createOk("success");
    }

    @PostMapping("/market/escrow")
    public ApiResponseDto<String> escrowNumber(@RequestBody ProductDto productDto) {
        bankService.escrowNumber(productDto);
        return ApiResponseDto.createOk("success");
    }

}