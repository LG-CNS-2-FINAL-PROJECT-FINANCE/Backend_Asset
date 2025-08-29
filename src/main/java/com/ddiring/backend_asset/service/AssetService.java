package com.ddiring.backend_asset.service;

import com.ddiring.backend_asset.common.exception.NotFound;
import com.ddiring.backend_asset.dto.UpdateAssetRequestDto; // Market에서 받은 DTO
import com.ddiring.backend_asset.entitiy.Wallet;
import com.ddiring.backend_asset.repository.BankRepository;
import com.ddiring.backend_asset.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final BankService bankService;
    private final TokenService tokenService;
    private final WalletRepository walletRepository;

    @Transactional
    public void updateAssetsAfterTrade(UpdateAssetRequestDto request) {

        Wallet buyerWallet = walletRepository.findByWalletAddress(request.getBuyAddress())
                .orElseThrow(() -> new NotFound("구매자 지갑 정보를 찾을 수 없습니다: " + request.getBuyAddress()));
        Wallet sellerWallet = walletRepository.findByWalletAddress(request.getSellAddress())
                .orElseThrow(() -> new NotFound("판매자 지갑 정보를 찾을 수 없습니다: " + request.getSellAddress()));

        tokenService.addBuyToken(buyerWallet.getUserSeq(), request.getProjectId(), request.getBuyTokenAmount());

        String sellerRole = "USER";
        
        bankService.depositForTrade(sellerWallet.getUserSeq(), sellerRole,  request.getSellPrice());
    }
}