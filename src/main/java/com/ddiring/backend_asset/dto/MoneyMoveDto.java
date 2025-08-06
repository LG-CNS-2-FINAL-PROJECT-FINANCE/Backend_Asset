package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.entitiy.History;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class MoneyMoveDto {
    private Integer bankPrice;
    private LocalDate bankTime;
    private Integer bankType;
    private Integer moneyType;

    public MoneyMoveDto(History history) {
        this.bankPrice = history.getBankPrice();
        this.bankTime = history.getBankTime();
        this.moneyType = history.getMoneyType();
        this.bankType = history.getBankType();
    }

}
