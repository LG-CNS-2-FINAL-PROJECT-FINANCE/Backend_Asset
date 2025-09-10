package com.ddiring.backend_asset.service;

import com.ddiring.backend_asset.api.market.MarketTokenDto;
import com.ddiring.backend_asset.common.exception.BadParameter;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;
    private final EscrowRepository escrowRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 2차거래 토큰 구매
    @Transactional
    public void addBuyToken(String userSeq, String projectId, Long amountToAdd , Integer price) {

        Optional<Token> tokenOptional = tokenRepository.findByUserSeqAndProjectId(userSeq, projectId);
        Escrow escrow = escrowRepository.findByProjectId(projectId) .orElseThrow(() -> new NotFound("해당 프로젝트의 토큰을 찾을 수 없습니다."));;
        if (tokenOptional.isPresent()) {
            Token existingToken = tokenOptional.get();
            existingToken.setAmount(existingToken.getAmount() + amountToAdd.intValue());
            tokenRepository.save(existingToken);
            existingToken.setPrice(existingToken.getAmount() * (int) (price / amountToAdd));
            tokenRepository.save(existingToken);
        } else {
            Token newToken = Token.builder()
                    .userSeq(userSeq)
                    .projectId(projectId)
                    .price(price)
                    .title(escrow.getTitle())
                    .currentPrice((int) (price / amountToAdd))
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

        if (token.getAmount() < marketSellDto.getSellToken()) {
            throw new BadParameter("토큰 없");
        }
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
            existingToken.setAmount(existingToken.getAmount() + marketTokenDto.getTokenQuantity());
        }
        else {
            log.info("유저 시퀀스 : {}", marketTokenDto.getUserSeq());
            Token newToken = Token.builder()
                    .userSeq(marketTokenDto.getUserSeq())
                    .projectId(projectId)
                    .price(marketTokenDto.getPerPrice() * marketTokenDto.getTokenQuantity())
                    .currentPrice(marketTokenDto.getPerPrice())
                    .title(escrow.getTitle())
                    .amount(marketTokenDto.getTokenQuantity())
                    .build();

            tokenRepository.save(newToken);
        }
    }

    @Transactional
    public void updateTokenCurrentPrice(String projectId, Integer newPrice) {
        List<Token> tokens = tokenRepository.findByProjectId(projectId);
        if (tokens.isEmpty()) {
            log.warn("가격 업데이트 대상 토큰을 찾을 수 없습니다. projectId={}", projectId);
            return;
        }

        tokens.forEach(token -> {
            token.setCurrentPrice(newPrice);
            token.setPrice(token.getAmount() * newPrice);
        });

        tokenRepository.saveAll(tokens);

        log.info("projectId {}의 토큰 {}개 현재가를 {}원으로 업데이트했습니다.", projectId, tokens.size(), newPrice);

        String destination = "/topic/price/" + projectId;
        Map<String, Object> payload = new HashMap<>();
        payload.put("projectId", projectId);
        payload.put("currentPrice", newPrice);

        messagingTemplate.convertAndSend(destination, payload);
        log.info("웹소켓 전송 완료: destination={}, payload={}", destination, payload);
    }
    @Transactional(readOnly = true)
    public boolean checkUserToken(String userSeq, String projectId, Integer requiredAmount) {
        Token token = tokenRepository.findByUserSeqAndProjectId(userSeq, projectId)
                .orElseThrow(() -> new NotFound("보유한 토큰이 없습니다."));
        return token.getAmount() >= requiredAmount;
    }
}
