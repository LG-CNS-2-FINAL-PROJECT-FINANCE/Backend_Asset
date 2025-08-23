package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.entitiy.Bank;
import com.ddiring.backend_asset.entitiy.History;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class DepositDto {
    private String userSeq;
    private String projectId;
    private String role;
    private Integer price;
}
