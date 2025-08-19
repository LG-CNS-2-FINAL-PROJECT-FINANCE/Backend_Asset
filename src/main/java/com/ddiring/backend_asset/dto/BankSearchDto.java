package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.entitiy.Bank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BankSearchDto {
    private String bankNumber;
    private Integer deposit;
    private Integer role;

    public BankSearchDto(Bank bank) {
        this.bankNumber = bank.getBankNumber();
        this.deposit = bank.getDeposit();
        this.role = bank.getRole();
    }
}
