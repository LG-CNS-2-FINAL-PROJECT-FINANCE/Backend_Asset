package com.ddiring.backend_asset.event.cosumer;

import com.ddiring.backend_asset.event.dto.InvestSucceededEvent;
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

//    private final ObjectMapper objectMapper;
//
//    @KafkaListener(topics = "INVESTMENT", groupId = "asset-service-group")
//    public void listenInvestmentEvents(String message) {
//        try {
//            Map<String, Object> messageMap = objectMapper.readValue(message, new TypeReference<>() {
//            });
//            String eventType = (String) messageMap.get("eventType");
//            if (eventType == null) {
//                log.warn("eventType 필드를 찾을 수 없습니다: {}", message);
//                return;
//            }
//
//            log.info("[INVEST] 이벤트 수신: {}", eventType);
//            switch (eventType) {
//                case "INVESTMENT.SUCCEEDED": {
//                    InvestSucceededEvent succeeded = objectMapper.convertValue(messageMap.get("payload"),
//                            InvestSucceededEvent.class);
//                    handleInvestSucceeded(succeeded);
//                    break;
//                }
//                default:
//                    log.warn("알 수 없는 투자 이벤트 타입: {}", eventType);
//                    break;
//            }
//        } catch (Exception e) {
//            log.error("투자 이벤트 처리 실패: {}", message, e);
//        }
//    }
//
//    private void handleInvestSucceeded(InvestSuc ceededEvent event) {
//        Long id = event.getPayload().getInvestmentId();
//        investmentRepository.findById(id.intValue()).ifPresent(inv -> {
//            if (inv.getInvStatus() != InvestmentStatus.COMPLETED) {
//                inv.setInvStatus(InvestmentStatus.COMPLETED);
//                inv.setUpdatedAt(LocalDateTime.now());
//                investmentRepository.save(inv);
//            }
//        });
//    }

}
