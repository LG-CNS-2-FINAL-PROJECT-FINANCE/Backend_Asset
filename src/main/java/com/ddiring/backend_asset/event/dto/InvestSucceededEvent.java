package com.ddiring.backend_asset.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class InvestSucceededEvent {
    public static final String PREFIX = "INVESTMENT";

    // --- Header ---
    private String eventId;
    private String eventType;
    private Instant timestamp;

    // --- Payload ---
    private InvestSucceededPayload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class InvestSucceededPayload {
        private Long investmentId;
        private String status;
        private String investorAddress;
        private Long tokenAmount;
        private String projectId;
    }

    public static InvestSucceededEvent of(Long investmentId, String investorAddress, Long tokenAmount, String projectId) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = PREFIX + ".SUCCEEDED";

        return InvestSucceededEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(InvestSucceededPayload.builder()
                        .investmentId(investmentId)
                        .status("SUCCEEDED")
                        .investorAddress(investorAddress)
                        .tokenAmount(tokenAmount)
                        .projectId(projectId)
                        .build())
                .build();
    }
}