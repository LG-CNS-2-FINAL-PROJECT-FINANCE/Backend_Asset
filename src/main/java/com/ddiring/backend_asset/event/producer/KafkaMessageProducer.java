package com.ddiring.backend_asset.event.producer;

import com.ddiring.backend_asset.event.dto.TokenNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private void sendMessage(String topic, Object message) {
        kafkaTemplate.send(topic, message);
    }

    public void sendInvestmentSucceededMessage(String title, String userSeq) {
        TokenNotificationEvent message = TokenNotificationEvent.ofInvestmentSucceeded(title, userSeq);
        sendMessage(TokenNotificationEvent.TOPIC, message);
        log.info("[InvestmentSucceeded] Sent message to {}: {}", TokenNotificationEvent.TOPIC, message);
    }

    public void sendInvestmentFailedMessage(String title, String userSeq) {
        TokenNotificationEvent message = TokenNotificationEvent.ofInvestmentFailed(title, userSeq);
        sendMessage(TokenNotificationEvent.TOPIC, message);
        log.info("[InvestmentFailed] Sent message to {}: {}", TokenNotificationEvent.TOPIC, message);
    }

    public void sendTradeSucceededMessage(String title, String userSeq) {
        TokenNotificationEvent message = TokenNotificationEvent.ofTradeSucceeded(title, userSeq);
        sendMessage(TokenNotificationEvent.TOPIC, message);
        log.info("[TradeSucceeded] Sent message to {}: {}", TokenNotificationEvent.TOPIC, message);
    }

    public void sendTradeFailedMessage(String title, String userSeq) {
        TokenNotificationEvent message = TokenNotificationEvent.ofTradeFailed(title, userSeq);
        sendMessage(TokenNotificationEvent.TOPIC, message);
        log.info("[TradeFailed] Sent message to {}: {}", TokenNotificationEvent.TOPIC, message);
    }

}
