package com.ddiring.backend_asset.event.consumer;

import com.ddiring.backend_asset.common.exception.NotFound;
import com.ddiring.backend_asset.entitiy.Escrow;
import com.ddiring.backend_asset.entitiy.Token;
import com.ddiring.backend_asset.entitiy.Wallet;
import com.ddiring.backend_asset.event.dto.InvestSucceededEvent;
import com.ddiring.backend_asset.repository.EscrowRepository;
import com.ddiring.backend_asset.repository.TokenRepository;
import com.ddiring.backend_asset.repository.WalletRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaInvestmentEventsListener {

    private final ObjectMapper objectMapper;
    private final EscrowRepository escrowRepository;
    private final WalletRepository walletRepository;
    private final TokenRepository tokenRepository;

    @KafkaListener(topics = "INVESTMENT", groupId = "asset-service-group")
    public void listenInvestmentEvents(String message) { // 파라미터를 String으로 변경
        try {
            // ObjectMapper를 사용하여 String 메시지를 Map으로 직접 변환
            Map<String, Object> messageMap = objectMapper.readValue(message, new TypeReference<>() {});
            String eventType = (String) messageMap.get("eventType");
            if (eventType == null) {
                log.warn("eventType 필드를 찾을 수 없습니다: {}", messageMap);
                return;
            }

            log.info("[INVEST] 이벤트 수신: {}", eventType);
            switch (eventType) {
                case "INVESTMENT.SUCCEEDED": {
                    // 전체 메시지 맵을 InvestSucceededEvent 객체로 변환
                    InvestSucceededEvent succeeded = objectMapper.convertValue(messageMap,
                            InvestSucceededEvent.class);
                    handleInvestSucceeded(succeeded);
                    break;
                }
                default:
                    log.warn("알 수 없는 투자 이벤트 타입: {}", eventType);
                    break;
            }
        } catch (Exception e) {
            log.error("투자 이벤트 처리 실패: {}", message, e); // 에러 로그에 message 출력
        }
    }

    @Transactional
    public void handleInvestSucceeded(InvestSucceededEvent event) {
        InvestSucceededEvent.InvestSucceededPayload payload = event.getPayload();
        if (payload == null) {
            log.warn("INVESTMENT.SUCCEEDED 이벤트에 payload가 없습니다.");
            return;
        }

        // investorAddress로 Wallet을 찾아 userSeq를 가져옴
        Wallet investorWallet = walletRepository.findByWalletAddress(payload.getInvestorAddress())
                .orElseThrow(() -> new NotFound("투자자 지갑을 찾을 수 없습니다: " + payload.getInvestorAddress()));
        String userSeq = investorWallet.getUserSeq();

        // 멱등성 체크: 해당 사용자가 프로젝트 토큰을 이미 보유하고 있는지 확인
        Optional<Token> existingTokenOpt = tokenRepository.findByUserSeqAndProjectId(userSeq, payload.getProjectId());

        if (existingTokenOpt.isPresent()) {
            log.info("이미 투자 토큰이 지급된 이벤트입니다. 중복 처리 방지. userSeq={}, projectId={}", userSeq, payload.getProjectId());
            return;
        }

        Escrow escrow = escrowRepository.findByProjectId(payload.getProjectId())
                .orElseThrow(() -> new NotFound("에스크로 정보를 찾을 수 없습니다: " + payload.getProjectId()));

        Token token = Token.builder()
                .title(escrow.getTitle())
                .build();

        tokenRepository.save(token);
        log.info("투자 성공 처리 완료: userSeq={}, projectId={}, amount={}", userSeq, payload.getProjectId(), payload.getTokenAmount());
    }
}
