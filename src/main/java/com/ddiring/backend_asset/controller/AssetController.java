package com.ddiring.backend_asset.controller;

import com.ddiring.backend_asset.api.market.MarketDto;
import com.ddiring.backend_asset.api.market.MarketTokenDto;
import com.ddiring.backend_asset.api.product.DistributionDto;
import com.ddiring.backend_asset.api.product.ProductDto;
import com.ddiring.backend_asset.common.dto.ApiResponseDto;
import com.ddiring.backend_asset.dto.*;
import com.ddiring.backend_asset.service.BankService;
import com.ddiring.backend_asset.service.TokenService;
import com.ddiring.backend_asset.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/asset")
@RequiredArgsConstructor
public class AssetController {
    private final BankService bankService;
    private final WalletService walletService;
    private final TokenService tokenService;

    @PostMapping("/account") //뱅크 생성
    public ApiResponseDto<String> createBank(@RequestHeader("userSeq") String userSeq,
                                             @RequestHeader("role") String role) {
        bankService.createBank(userSeq, role);
        return ApiResponseDto.createOk("물주 생성 굿");
    }

    @GetMapping("/account/search")
    public ApiResponseDto<BankSearchDto> bankSearch(@RequestHeader("userSeq") String userSeq,
                                                    @RequestHeader("role") String role) {
        BankSearchDto history = bankService.bankSearch(userSeq, role);
        return ApiResponseDto.createOk(history);
    }

    @PostMapping("/account/deposit") //입금
    public ApiResponseDto<Integer> deposit(@RequestHeader("userSeq") String userSeq,
                                           @RequestHeader("role") String role,
                                           @RequestBody DepositDto depositDto) {
        bankService.deposit(userSeq, role, depositDto);
        return ApiResponseDto.createOk(depositDto.getPrice());
    }

    @PostMapping("/account/withdrawal")
    public ApiResponseDto<Integer> withdrawal(@RequestHeader("userSeq") String userSeq,
                                              @RequestHeader("role") String role,
                                              @RequestBody WithdrawalDto withdrawalDto) {
        bankService.withdrawal(userSeq, role, withdrawalDto);
        return ApiResponseDto.createOk(withdrawalDto.getWithdrawal());
    }

    @PostMapping("/wallet")
    public ApiResponseDto<String> createWallet(@RequestHeader("userSeq") String userSeq) {
        walletService.createWalletAndReturnKeys(userSeq);
        return ApiResponseDto.createOk("지갑 생성 완");
    }

    @GetMapping("/wallet/search")
    public ApiResponseDto<String> getWalletAddress(@RequestHeader("userSeq") String userSeq) {
        String walletTokenInfoList = walletService.getWalletAddress(userSeq);
        return ApiResponseDto.createOk(walletTokenInfoList);
    }

    @GetMapping("/wallet-token/search")
    public ApiResponseDto<List<WalletTokenInfoDto>> getTokenAmount(@RequestHeader("userSeq") String userSeq) {
        List<WalletTokenInfoDto> walletTokenInfoList = tokenService.getTokenInfo(userSeq);
        return ApiResponseDto.createOk(walletTokenInfoList);
    }


    @GetMapping("/history/{moneyType}")
    public ApiResponseDto<List<MoneyMoveDto>> history(@RequestHeader("userSeq") String userSeq,
                                                      @RequestHeader("role") String role,
                                                      @PathVariable Integer moneyType) {
        List<MoneyMoveDto> history = bankService.moneyMove(userSeq, role, moneyType);
        return ApiResponseDto.createOk(history);
    }

    @GetMapping("/history")
    public ApiResponseDto<List<MoneyMoveDto>> allhistory(@RequestHeader("userSeq") String userSeq,
                                                         @RequestHeader("role") String role) {
        List<MoneyMoveDto> history = bankService.allMoneyMove(userSeq, role);
        return ApiResponseDto.createOk(history);
    }

    @PostMapping("/escrow/account")
    public ApiResponseDto<String> registerEscrow(@RequestBody ProductDto productDto) {
        bankService.escrowAccount(productDto);
        return ApiResponseDto.defaultOk();
    }

    @PostMapping("/escrow/deposit")
    public ApiResponseDto<Integer> depositToEscrow(@RequestHeader("userSeq") String userSeq,
                                                   @RequestHeader("role") String role,
                                                   @RequestBody MarketDto marketDto) {
        Integer money = bankService.depositToEscrow(userSeq, role, marketDto);
        return ApiResponseDto.createOk(money);
    }

    @PostMapping("/escrow/withdrawal")
    public ApiResponseDto<Integer> withdrawalFromEscrow(@RequestHeader("userSeq") String userSeq,
                                                        @RequestHeader("role") String role,
                                                        @RequestBody MarketDto marketDto) {
        Integer money = bankService.withdrawalFromEscrow(userSeq, role, marketDto);
        return ApiResponseDto.createOk(money);
    }

    @GetMapping("/bank/balance")
    public ApiResponseDto<BankSearchDto> getBankBalance(@RequestParam("userSeq") String userSeq, @RequestParam("role") String role) {
        BankSearchDto bankSearchDto = bankService.bankSearch(userSeq, role);
        return ApiResponseDto.createOk(bankSearchDto);
    }

    @PostMapping("/market/buy")
    public ApiResponseDto<String> marketBuy(@RequestHeader("userSeq") String userSeq,
                                            @RequestHeader("role") String role,
                                            @RequestBody MarketBuyDto marketBuyDto) {
        bankService.setBuyPrice(userSeq, role, marketBuyDto);
        return ApiResponseDto.createOk("success");
    }

    @PostMapping("/market/sell")
    public ApiResponseDto<String> marketSell(@RequestHeader("userSeq") String userSeq,
                                             @RequestBody MarketSellDto marketSellDto) {
        tokenService.setSellToken(userSeq, marketSellDto);
        return ApiResponseDto.createOk("success");
    }

    @PostMapping("/market/refund")
    public ApiResponseDto<String> marketRefund(@RequestHeader("userSeq") String userSeq,
                                               @RequestHeader("role") String role,
                                               @RequestBody MarketRefundDto marketRefundDto) {
        bankService.setRefundToken(userSeq, role, marketRefundDto);
        return ApiResponseDto.createOk("success");
    }

    @PostMapping("/title")
    public String getMarketTitle(@RequestBody TitleRequestDto requestDto) {
        return bankService.getMarketTitleDto(requestDto.getProjectId());
    }

    @PostMapping("/market/profit")
    public ApiResponseDto<String> marketProfit(@RequestHeader("userSeq") String userSeq,
                                               @RequestHeader("role") String role,
                                               @RequestBody MarketBuyDto marketBuyDto) {
        bankService.setprofit(userSeq, role, marketBuyDto);
        return ApiResponseDto.createOk("success");
    }

    @PostMapping("/all")
    public Integer getAllMoney(@RequestHeader("userSeq") String userSeq,
                               @RequestHeader("role") String role,
                               @RequestBody AssetAllMoneyDto assetAllMoneyDto) {
        return bankService.getAllMoney(userSeq, role, assetAllMoneyDto);
    }

    @PostMapping("/get/token/{projectId}")
    public void getToken(@PathVariable String projectId, @RequestBody MarketTokenDto marketTokenDto) {
        tokenService.getToken(projectId, marketTokenDto);
    }

    @PostMapping("/distribution")
    public void getDistribution(@RequestBody DistributionDto distributionDto) {
        bankService.getDistribution(distributionDto);
    }

    @GetMapping("/wallet/private-key")
    public ApiResponseDto<String> getDecryptedPrivateKey(@RequestHeader("userSeq") String userSeq) {
        String privateKey = walletService.getDecryptedPrivateKey(userSeq);
        return ApiResponseDto.createOk(privateKey);
    }

    @PostMapping("/market/check-balance")
    public ApiResponseDto<Boolean> checkBalance(@RequestHeader("userSeq") String userSeq, @RequestHeader("role") String role, @RequestBody MarketBuyDto marketBuyDto) {
        boolean hasEnoughBalance = bankService.checkUserBalance(userSeq, role, marketBuyDto.getBuyPrice());
        return ApiResponseDto.createOk(hasEnoughBalance);
    }

    @PostMapping("/market/check-token")
    public ApiResponseDto<Boolean> checkToken(@RequestHeader("userSeq") String userSeq, @RequestBody MarketSellDto marketSellDto) {
        boolean hasEnoughTokens = tokenService.checkUserToken(userSeq, marketSellDto.getProjectId(), marketSellDto.getSellToken());
        return ApiResponseDto.createOk(hasEnoughTokens);
    }
}

