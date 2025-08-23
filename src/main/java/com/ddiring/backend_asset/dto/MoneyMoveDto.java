package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.entitiy.History;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MoneyMoveDto {
    private Integer bankPrice;
    private LocalDateTime bankTime;
    private Integer moneyType;
    public MoneyMoveDto(History history) {
        this.bankPrice = history.getBankPrice();
        this.bankTime = history.getBankTime();
        this.moneyType = history.getMoneyType();
    }

}
