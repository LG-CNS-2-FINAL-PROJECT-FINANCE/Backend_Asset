package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.entitiy.Bank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BankSearchDto {
    public String bankNumber;
    public Long deposit;
    public BankSearchDto(Bank bank) {
        this.bankNumber = bank.getBankNumber();
        this.deposit = bank.getDeposit();
    }
}
