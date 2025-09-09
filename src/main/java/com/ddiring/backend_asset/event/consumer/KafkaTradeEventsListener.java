package com.ddiring.backend_asset.event.consumer;

import com.ddiring.backend_asset.api.market.MarketClient;
import com.ddiring.backend_asset.api.market.TradeInfoResponseDto;
import com.ddiring.backend_asset.common.dto.ApiResponseDto;
import com.ddiring.backend_asset.common.exception.NotFound;
import com.ddiring.backend_asset.dto.MarketRefundDto;
import com.ddiring.backend_asset.entitiy.Wallet;
import com.ddiring.backend_asset.event.dto.TradeFailedEvent;
import com.ddiring.backend_asset.event.dto.TradeSucceededEvent;
import com.ddiring.backend_asset.event.dto.TradePriceUpdateEvent;
import com.ddiring.backend_asset.repository.WalletRepository;
import com.ddiring.backend_asset.service.BankService;
import com.ddiring.backend_asset.service.TokenService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTradeEventsListener {

    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final WalletRepository walletRepository;
    private final BankService bankService;
    private final MarketClient marketClient;

    /**
     * TRADE 토픽에서 발생하는 모든 이벤트를 수신하여 처리합니다.
     */
    @KafkaListener(topics = "TRADE", groupId = "asset-service-group")
    public void listenTradeEvents(String message) { // 파라미터를 String으로 변경
        try {
            // ObjectMapper를 사용하여 String 메시지를 Map으로 직접 변환
            Map<String, Object> messageMap = objectMapper.readValue(message, new TypeReference<>() {});
            String eventType = (String) messageMap.get("eventType");
            if (eventType == null) {
                log.warn("eventType 필드를 찾을 수 없습니다: {}", messageMap);
                return;
            }

            log.info("수신된 이벤트 타입: {}", eventType);
            switch (eventType) {
                case "TRADE.SUCCEEDED":
                    TradeSucceededEvent succeededEvent = objectMapper.convertValue(messageMap, TradeSucceededEvent.class);
                    handleTradeSucceeded(succeededEvent);
                    break;
                case "TRADE.FAILED":
                    TradeFailedEvent failedEvent = objectMapper.convertValue(messageMap, TradeFailedEvent.class);
                    handleTradeFailed(failedEvent);
                    break;
                case "TRADE.PRICE.UPDATED":
                    TradePriceUpdateEvent priceUpdateEvent = objectMapper.convertValue(messageMap, TradePriceUpdateEvent.class);
                    handleTradePriceUpdate(priceUpdateEvent);
                    break;
                default:
                    log.warn("알 수 없는 이벤트 타입입니다: {}", eventType);
                    break;
            }
        } catch (Exception e) {
            log.error("Kafka 메시지 처리 중 오류 발생: {}", message, e); // 에러 로그에 messageMap 대신 message 출력
        }
    }

    @Transactional
    public void handleTradeSucceeded(TradeSucceededEvent event) {
        TradeSucceededEvent.TradeSucceededPayload payload = event.getPayload();
        log.info("TradeSucceededEvent 처리 시작: tradeId={}", payload.getTradeId());

        try {
            // 멱등성 처리를 위해 Market 서비스에서 현재 거래 상태를 먼저 조회합니다.
            ApiResponseDto<TradeInfoResponseDto> response = marketClient.getTradeInfo(payload.getTradeId());
            TradeInfoResponseDto tradeInfo = response.getData();

            // 멱등성 체크: 이미 처리된 거래인지 확인합니다.
            if ("SUCCEEDED".equals(tradeInfo.getStatus())) {
                log.info("이미 'SUCCEEDED' 상태인 거래입니다. 중복 이벤트이므로 무시합니다. tradeId={}", payload.getTradeId());
                return;
            }

            Wallet buyerWallet = walletRepository.findByWalletAddress(payload.getBuyerAddress())
                    .orElseThrow(() -> new NotFound("구매자 지갑을 찾을 수 없습니다: " + payload.getBuyerAddress()));
            Wallet sellerWallet = walletRepository.findByWalletAddress(payload.getSellerAddress())
                    .orElseThrow(() -> new NotFound("판매자 지갑을 찾을 수 없습니다: " + payload.getSellerAddress()));

            tokenService.addBuyToken(buyerWallet.getUserSeq(), payload.getProjectId(), payload.getTradeAmount(), tradeInfo.getPrice());
            bankService.depositForTrade(sellerWallet.getUserSeq(), "USER", tradeInfo.getPrice());
            log.info("자산 이동 처리 완료. tradeId={}", payload.getTradeId());

        } catch (Exception e) {
            log.error("TradeSucceededEvent 처리 중 심각한 오류 발생. tradeId={}", payload.getTradeId(), e);
            throw new RuntimeException("TradeSucceededEvent 처리 실패", e);
        }
    }

    /**
     * 거래 실패 이벤트를 처리합니다. (자산 원복)
     */
    @Transactional
    public void handleTradeFailed(TradeFailedEvent event) {
        TradeFailedEvent.TradeFailedPayload payload = event.getPayload();
        log.info("TradeFailedEvent 처리 시작: tradeId={}", payload.getTradeId());

        try {
            // 멱등성 처리를 위해 Market 서비스에서 현재 거래 상태를 먼저 조회합니다.
            ApiResponseDto<TradeInfoResponseDto> response = marketClient.getTradeInfo(payload.getTradeId());
            TradeInfoResponseDto tradeInfo = response.getData();

            // 멱등성 체크: 이미 처리된 거래인지 확인합니다.
            if ("FAILED".equals(tradeInfo.getStatus())) {
                log.info("이미 'FAILED' 상태인 거래입니다. 중복 이벤트이므로 무시합니다. tradeId={}", payload.getTradeId());
                return;
            }

            // --- 실제 자산 원복 로직 ---
            tokenService.addBuyToken(tradeInfo.getSellerUserSeq(), tradeInfo.getProjectId(), (long) tradeInfo.getTokenQuantity(), tradeInfo.getPrice());

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

    public void handleTradePriceUpdate(TradePriceUpdateEvent event) {
        TradePriceUpdateEvent.Payload payload = event.getPayload();
        if (payload != null && payload.getProjectId() != null && payload.getPricePerToken() != null) {
            tokenService.updateTokenCurrentPrice(payload.getProjectId(), payload.getPricePerToken());
        } else {
            log.warn("가격 업데이트 이벤트의 payload가 유효하지 않습니다: {}", event);
        }
    }
}
