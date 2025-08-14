package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.entitiy.Bank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateBankDto {
    private Integer bankType;
    private Integer roll;
    private String bankNumber;
    public CreateBankDto(Bank bank) {
        this.bankNumber = bank.getBankNumber();
    }
}
