package com.ddiring.backend_asset.service;

import com.ddiring.backend_asset.common.exception.NotFound;
import com.ddiring.backend_asset.dto.MarketRefundDto;
import com.ddiring.backend_asset.dto.MarketSellDto;
import com.ddiring.backend_asset.dto.WalletTokenInfoDto;
import com.ddiring.backend_asset.entitiy.Token;
import com.ddiring.backend_asset.entitiy.Wallet;
import com.ddiring.backend_asset.repository.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;

//    @Transactional
//    public void searchToken(String userSeq, WalletTokenInfoDto walletTokenInfoDto) {
//        Token token = tokenRepository.findByUserSeq(userSeq)
//                .orElseThrow(() -> new NotFound("누구?"));
//
//        Token token1 = Token.builder()
//                .projectId(token.getProjectId())
//                .amount(token.getAmount())
//                .price(token.getPrice())
//                .build();
//        re
//    }

    @Transactional
    public void setSellToken(String userSeq, MarketSellDto marketSellDto) {
        Token token = tokenRepository.findByUserSeqAndTokenSymbol(userSeq, marketSellDto.getTokenSymbol())
                .orElseThrow(() -> new NotFound("누구?"));

        token.setAmount(token.getAmount() - marketSellDto.getSellToken());
        tokenRepository.save(token);
    }

    @Transactional
    public List<WalletTokenInfoDto> getTokenInfo(String userSeq) {
        List<Token> tokens = tokenRepository.findByUserSeq(userSeq);
        if(tokens.isEmpty()){
            List.of();
        }

        return tokens.stream()
                .map(WalletTokenInfoDto::new)
                .collect(Collectors.toList());
    }
}
