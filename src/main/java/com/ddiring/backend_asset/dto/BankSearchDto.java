package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.entitiy.Bank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BankSearchDto {
    private Integer bankSeq;
    private Integer deposit;

    public BankSearchDto(Bank bank) {
        this.bankSeq = bank.getBankSeq();
        this.deposit = bank.getDeposit();
    }
}
