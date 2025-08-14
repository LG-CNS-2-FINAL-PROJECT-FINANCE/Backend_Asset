package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.entitiy.EscrowHistory;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class EscrowHistroyDto {
    private Integer price;
    private Integer trasferType;
    private LocalDate tansferDate;

    public EscrowHistroyDto(EscrowHistory escrowHistory) {
        this.price = escrowHistory.getPrice();
        this.trasferType = escrowHistory.getTransferType();
        this.tansferDate = escrowHistory.getTransferDate();
    }
}
