package com.ddiring.backend_asset.event.cosumer;

import com.ddiring.backend_asset.api.market.MarketClient;
import com.ddiring.backend_asset.api.market.TradeInfoResponseDto;
import com.ddiring.backend_asset.common.dto.ApiResponseDto;
import com.ddiring.backend_asset.common.exception.NotFound;
import com.ddiring.backend_asset.dto.MarketRefundDto;
import com.ddiring.backend_asset.entitiy.Wallet;
import com.ddiring.backend_asset.event.dto.TradeFailedEvent;
import com.ddiring.backend_asset.event.dto.TradeSucceededEvent;
import com.ddiring.backend_asset.repository.BankRepository;
import com.ddiring.backend_asset.repository.TokenRepository;
import com.ddiring.backend_asset.repository.WalletRepository;
import com.ddiring.backend_asset.service.BankService;
import com.ddiring.backend_asset.service.TokenService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTradeEventsListener {

    private final TokenRepository tokenRepository;
    private final WalletRepository walletRepository;
    private final BankService bankService;
    private final MarketClient marketClient;
    private final ObjectMapper objectMapper; // JSON 파싱을 위해 ObjectMapper 주입
    private final TokenService tokenService;

    @KafkaListener(topics = "TRADE", groupId = "asset-service-group")
    public void listenTradeEvents(String message) {
        try {

            Map<String, Object> messageMap = objectMapper.readValue(message, new TypeReference<>() {});

            String eventType = (String) messageMap.get("eventType");
            if (eventType == null) {
                log.warn("eventType 필드를 찾을 수 없습니다: {}", message);
                return;
            }

            log.info("수신된 이벤트 타입: {}", eventType);
            switch (eventType) {
                case "TRADE.SUCCEEDED":
                    TradeSucceededEvent tradeSucceededEvent = objectMapper.convertValue(messageMap, TradeSucceededEvent.class);
                    log.info(tradeSucceededEvent.toString());
                    handleTradeSucceeded(tradeSucceededEvent);
                    log.info(tradeSucceededEvent.toString());
                    break;
                case "TRADE.FAILED":
                    TradeFailedEvent tradeFailedEvent = objectMapper.convertValue(messageMap, TradeFailedEvent.class);
                    log.info(tradeFailedEvent.toString());
                    handleTradeFailed(tradeFailedEvent);
                    log.info(tradeFailedEvent.toString());
                    break;
                default:
                    log.warn("알 수 없는 이벤트 타입입니다: {}", eventType);
                    break;
            }
        } catch (Exception e) {
            log.error("Kafka 메시지 처리 중 오류 발생: {}", message, e);
        }
    }

    @Transactional
    public void handleTradeSucceeded(TradeSucceededEvent event) {
        TradeSucceededEvent.TradeSucceededPayload payload = event.getPayload();
        log.info("TradeSucceededEvent 처리 시작: tradeId={}", payload.getTradeId());

        Wallet wallet1 = walletRepository.findByWalletAddress(payload.getBuyerAddress()).orElseThrow(() -> new NotFound("who are you?"));
        Wallet wallet = walletRepository.findByWalletAddress(payload.getSellerAddress()).orElseThrow(() -> new NotFound("who are you?"));

        try {
            ApiResponseDto<TradeInfoResponseDto> response = marketClient.getTradeInfo(payload.getTradeId());
            TradeInfoResponseDto tradeInfo = response.getData();
            tokenService.addBuyToken(wallet1.getUserSeq(), payload.getProjectId(), payload.getBuyerTokenAmount());
            bankService.depositForTrade(wallet.getUserSeq(), "USER", tradeInfo.getPrice());

        } catch (Exception e) {
            log.error("Asset 서비스 호출 중 심각한 오류 발생. tradeId={}", payload.getTradeId(), e);
            throw new RuntimeException("Asset 서비스 호출 실패", e);
        }
    }
    @Transactional
    public void handleTradeFailed(TradeFailedEvent event) {
        TradeFailedEvent.TradeFailedPayload payload = event.getPayload();
        log.info("TradeFailedEvent 처리: tradeId={}", payload.getTradeId());

        try {
            ApiResponseDto<TradeInfoResponseDto> response = marketClient.getTradeInfo(payload.getTradeId());
            TradeInfoResponseDto tradeInfo = response.getData();

            tokenService.addBuyToken(tradeInfo.getSellerUserSeq(), tradeInfo.getProjectId(), (long) tradeInfo.getTokenQuantity());

            MarketRefundDto marketRefundDto = MarketRefundDto.builder()
                    .refundPrice(tradeInfo.getPrice())
                    .ordersId(payload.getTradeId().intValue())
                    .projectId(tradeInfo.getProjectId())
                    .build();

            bankService.setRefundToken(tradeInfo.getBuyerUserSeq(), "USER", marketRefundDto);

            log.info("거래 실패(tradeId:{})로 인한 자산 원복 완료.", payload.getTradeId());

        } catch (Exception e) {
            log.error("자산 원복 처리 중 오류 발생. tradeId={}", payload.getTradeId(), e);
            throw new RuntimeException("자산 원복 처리 실패", e);
        }
    }
}