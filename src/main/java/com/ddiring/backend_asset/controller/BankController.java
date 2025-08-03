package com.ddiring.backend_asset.controller;

import com.ddiring.backend_asset.common.dto.ApiResponseDto;
import com.ddiring.backend_asset.dto.BankSearchDto;
import com.ddiring.backend_asset.dto.CreateBankDto;
import com.ddiring.backend_asset.dto.DepositDto;
import com.ddiring.backend_asset.dto.WithdrawalDto;
import com.ddiring.backend_asset.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/asset")
@RequiredArgsConstructor
public class BankController {
    private final BankService bankService;

    @PostMapping("/account")
    public ApiResponseDto<String> createBank(@RequestBody CreateBankDto createBankDto) {
        bankService.createBank(createBankDto);
        return ApiResponseDto.defaultOk();
    }

    @GetMapping("/{userId}/account")
    public ApiResponseDto<BankSearchDto> bankSearch(@PathVariable Integer userId) {
        BankSearchDto history = bankService.bankSearch(userId);
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
}
