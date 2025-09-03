package com.ddiring.backend_asset.service;

import com.ddiring.backend_asset.api.market.MarketTokenDto;
import com.ddiring.backend_asset.common.exception.NotFound;
import com.ddiring.backend_asset.common.util.GatewayRequestHeaderUtils;
import com.ddiring.backend_asset.dto.MarketRefundDto;
import com.ddiring.backend_asset.dto.MarketSellDto;
import com.ddiring.backend_asset.dto.WalletTokenInfoDto;
import com.ddiring.backend_asset.entitiy.Escrow;
import com.ddiring.backend_asset.entitiy.Token;
import com.ddiring.backend_asset.entitiy.Wallet;
import com.ddiring.backend_asset.repository.EscrowRepository;
import com.ddiring.backend_asset.repository.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;
    private final EscrowRepository escrowRepository;

    // 2차거래 토큰 구매
    @Transactional
    public void addBuyToken(String userSeq, String projectId, Long amountToAdd) {

        Optional<Token> tokenOptional = tokenRepository.findByUserSeqAndProjectId(userSeq, projectId);

        if (tokenOptional.isPresent()) {
            // 이미 토큰을 보유한 경우, 수량만 증가시킵니다.
            Token existingToken = tokenOptional.get();
            existingToken.setAmount(existingToken.getAmount() + amountToAdd.intValue());
            tokenRepository.save(existingToken);
        } else {

            Token newToken = Token.builder()
                    .userSeq(userSeq)
                    .projectId(projectId)
                    .amount(amountToAdd.intValue())
                    .build();
            tokenRepository.save(newToken);
        }
    }


    // 2차거래 토큰 판매 요청
    @Transactional
    public Integer setSellToken(String userSeq, MarketSellDto marketSellDto) {
        Token token = tokenRepository.findByUserSeqAndProjectId(userSeq, marketSellDto.getProjectId())
                .orElseThrow(() -> new NotFound("해당 프로젝트의 토큰을 찾을 수 없습니다."));

        token.setAmount(token.getAmount() - marketSellDto.getSellToken());
        tokenRepository.save(token);
        return token.getAmount();
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

    @Transactional
    public void getToken(String projectId, MarketTokenDto marketTokenDto) {
        log.info("유저 시퀀스 : {}", marketTokenDto.getUserSeq());
        Optional<Token> token = tokenRepository.findByUserSeqAndProjectId(marketTokenDto.getUserSeq(), projectId);

        Escrow escrow = escrowRepository.findByProjectId(projectId).orElseThrow(() -> new NotFound("해당 프로젝트를 찾을 수 없습니다."));

        if (token.isPresent()) {
            Token existingToken = token.get();

            long currentAmount = existingToken.getAmount() == null ? 0L : existingToken.getAmount();

            long newAmount = currentAmount + marketTokenDto.getTokenQuantity();

            existingToken.setAmount((int) newAmount);
        }
        else {
            log.info("유저 시퀀스 : {}", marketTokenDto.getUserSeq());
            Token newToken = Token.builder()
                    .userSeq(marketTokenDto.getUserSeq())
                    .projectId(projectId)
                    .price(marketTokenDto.getPerPrice())
                    .title(escrow.getTitle())
                    .amount(marketTokenDto.getTokenQuantity())
                    .build();

            tokenRepository.save(newToken);
        }
    }
}
