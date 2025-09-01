package com.ddiring.backend_asset.event.cosumer;

import com.ddiring.backend_asset.common.exception.NotFound;
import com.ddiring.backend_asset.common.util.GatewayRequestHeaderUtils;
import com.ddiring.backend_asset.entitiy.Escrow;
import com.ddiring.backend_asset.entitiy.Token;
import com.ddiring.backend_asset.event.dto.InvestSucceededEvent;
import com.ddiring.backend_asset.repository.EscrowRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaInvestmentEventsListener {

    private final ObjectMapper objectMapper;
    private final EscrowRepository escrowRepository;

    @KafkaListener(topics = "INVESTMENT", groupId = "asset-service-group")
    public void listenInvestmentEvents(String message) {
        try {
            Map<String, Object> messageMap = objectMapper.readValue(message, new TypeReference<>() {
            });
            String eventType = (String) messageMap.get("eventType");
            if (eventType == null) {
                log.warn("eventType 필드를 찾을 수 없습니다: {}", message);
                return;
            }

            log.info("[INVEST] 이벤트 수신: {}", eventType);
            switch (eventType) {
                case "INVESTMENT.SUCCEEDED": {
                    InvestSucceededEvent succeeded = objectMapper.convertValue(messageMap.get("payload"),
                            InvestSucceededEvent.class);
                    handleInvestSucceeded(succeeded);
                    break;
                }
                default:
                    log.warn("알 수 없는 투자 이벤트 타입: {}", eventType);
                    break;
            }
        } catch (Exception e) {
            log.error("투자 이벤트 처리 실패: {}", message, e);
        }
    }

    private void handleInvestSucceeded(InvestSucceededEvent event) {
        Escrow escrow = escrowRepository.findByProjectId(event.getPayload().getProjectId()).orElseThrow(() -> new NotFound("없"));
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        Token token = Token.builder()
                .amount(event.getPayload().getTokenAmount().intValue())
                .projectId(event.getPayload().getProjectId())
                .price(event.getPayload().getPrice().intValue())
                .userSeq(userSeq)
                .title(escrow.getTitle())
                .build();

    }

}
