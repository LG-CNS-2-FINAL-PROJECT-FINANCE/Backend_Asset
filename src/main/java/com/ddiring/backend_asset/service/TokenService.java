package com.ddiring.backend_asset.service;

import com.ddiring.backend_asset.common.exception.NotFound;
import com.ddiring.backend_asset.dto.MarketSellDto;
import com.ddiring.backend_asset.entitiy.Token;
import com.ddiring.backend_asset.entitiy.Wallet;
import com.ddiring.backend_asset.repository.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;

    @Transactional
    public void setSellToken(String userSeq, MarketSellDto marketSellDto) {
        Token token = tokenRepository.findByUserSeq(userSeq)
                .orElseThrow(() -> new NotFound("누구?"));

        token.setAmount(token.getAmount() - marketSellDto.getSellToken());
        tokenRepository.save(token);
    }
}
