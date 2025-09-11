package com.ddiring.backend_asset.event.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TokenNotificationEvent {
    public static final String TOPIC = "NOTIFICATION";

    private String eventId;
    private String eventType;
    private Instant timestamp;
    private TokenNotificationPayload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class TokenNotificationPayload {
        private String title;
        private String userSeq;
    }

    public static TokenNotificationEvent ofInvestmentSucceeded(String title, String userSeq) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".INVESTMENT.SUCCEEDED";

        return TokenNotificationEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(
                    TokenNotificationPayload.builder()
                        .title(title)
                        .userSeq(userSeq)
                        .build()
                )
                .build();
    }

    public static TokenNotificationEvent ofInvestmentFailed(String title, String userSeq) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".INVESTMENT.FAILED";

        return TokenNotificationEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(
                    TokenNotificationPayload.builder()
                        .title(title)
                        .userSeq(userSeq)
                        .build()
                )
                .build();
    }

    public static TokenNotificationEvent ofTradeSucceeded(String title, String userSeq) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".TRADE.SUCCEEDED";

        return TokenNotificationEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(
                    TokenNotificationPayload.builder()
                        .title(title)
                        .userSeq(userSeq)
                        .build()
                )
                .build();
    }

    public static TokenNotificationEvent ofTradeFailed(String title, String userSeq) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".TRADE.FAILED";

        return TokenNotificationEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(
                    TokenNotificationPayload.builder()
                        .title(title)
                        .userSeq(userSeq)
                        .build()
                )
                .build();
    }
}
