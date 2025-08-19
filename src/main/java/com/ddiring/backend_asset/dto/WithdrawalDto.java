package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.entitiy.Bank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WithdrawalDto {
    private Integer withdrawal;
    private String userSeq;
    private Integer role;
    public WithdrawalDto(Bank bank) {
        this.role = bank.getRole();
    }
}
