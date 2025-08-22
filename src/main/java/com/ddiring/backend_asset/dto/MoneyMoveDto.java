package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.entitiy.History;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class MoneyMoveDto {
    private Long bankPrice;
    private LocalDate bankTime;
    public MoneyMoveDto(History history) {
        this.bankPrice = history.getBankPrice();
        this.bankTime = history.getBankTime();
    }

}
