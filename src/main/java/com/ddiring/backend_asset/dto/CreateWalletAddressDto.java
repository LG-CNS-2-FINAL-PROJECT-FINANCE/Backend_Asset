package com.ddiring.backend_asset.dto;

import com.ddiring.backend_asset.entitiy.Wallet;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateWalletAddressDto {
    private String walletAddress;
    private String privateKey;

    public CreateWalletAddressDto (String walletAddress, String privateKey) {
        this.walletAddress = walletAddress;
        this.privateKey = privateKey;
    }
}
