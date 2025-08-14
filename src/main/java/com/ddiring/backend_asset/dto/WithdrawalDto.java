package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.entitiy.Bank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WithdrawalDto {
    private Integer withdrawal;
    private Integer userSeq;
    private Integer roll;
    public WithdrawalDto(Bank bank) {
        this.roll = bank.getRoll();
    }
}
