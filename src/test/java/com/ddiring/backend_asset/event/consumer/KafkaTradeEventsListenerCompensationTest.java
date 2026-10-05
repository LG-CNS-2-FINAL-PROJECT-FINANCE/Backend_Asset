package com.ddiring.backend_asset.event.consumer;

import com.ddiring.backend_asset.api.escrow.EscrowClient;
import com.ddiring.backend_asset.api.market.MarketClient;
import com.ddiring.backend_asset.api.market.TradeInfoResponseDto;
import com.ddiring.backend_asset.common.dto.ApiResponseDto;
import com.ddiring.backend_asset.entitiy.Escrow;
import com.ddiring.backend_asset.entitiy.Wallet;
import com.ddiring.backend_asset.event.dto.TradeFailedEvent;
import com.ddiring.backend_asset.event.producer.KafkaMessageProducer;
import com.ddiring.backend_asset.repository.BankRepository;
import com.ddiring.backend_asset.repository.EscrowRepository;
import com.ddiring.backend_asset.repository.HistoryRepository;
import com.ddiring.backend_asset.repository.TokenRepository;
import com.ddiring.backend_asset.repository.WalletRepository;
import com.ddiring.backend_asset.service.BankService;
import com.ddiring.backend_asset.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TRADE.FAILED 보상 트랜잭션(자산 원복) 검증 - N건 반복 실패 시나리오 테스트.
 * 실제 인프라(Kafka/DB/타 서비스) 없이, handleTradeFailed()가 실제로 하는 일을
 * Mockito로 경계(Repository/Feign Client)만 대체해 직접 호출·관찰한다.
 */
class KafkaTradeEventsListenerCompensationTest {

    @Test
    void 거래_실패_시_N건_보상_트랜잭션_결과를_집계한다() {
        int totalTrials = Integer.parseInt(System.getenv().getOrDefault("TRIALS", "1000"));
        int restored = 0;
        int failed = 0;

        for (int i = 1; i <= totalTrials; i++) {
            long tradeId = 1000L + i;
            String projectId = "project-" + i;
            String buyerUserSeq = "buyer-" + i;
            String sellerUserSeq = "seller-" + i;
            String buyerAddress = "0xBUYER" + i;
            String sellerAddress = "0xSELLER" + i;
            int price = 100_000 + i * 1000;
            int tokenQuantity = 10 + i;

            WalletRepository walletRepository = mock(WalletRepository.class);
            EscrowRepository escrowRepository = mock(EscrowRepository.class);
            TokenRepository tokenRepository = mock(TokenRepository.class);
            BankRepository bankRepository = mock(BankRepository.class);
            HistoryRepository historyRepository = mock(HistoryRepository.class);
            MarketClient marketClient = mock(MarketClient.class);
            EscrowClient escrowClient = mock(EscrowClient.class);
            KafkaMessageProducer kafkaMessageProducer = mock(KafkaMessageProducer.class);
            SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
            ObjectMapper objectMapper = new ObjectMapper();

            when(walletRepository.findByWalletAddress(buyerAddress))
                    .thenReturn(Optional.of(Wallet.builder().userSeq(buyerUserSeq).walletAddress(buyerAddress).build()));
            when(walletRepository.findByWalletAddress(sellerAddress))
                    .thenReturn(Optional.of(Wallet.builder().userSeq(sellerUserSeq).walletAddress(sellerAddress).build()));

            Escrow escrow = Escrow.builder().title("테스트 프로젝트 " + i).projectId(projectId).account("ACC-" + i).build();
            when(escrowRepository.findByProjectId(projectId)).thenReturn(Optional.of(escrow));

            when(tokenRepository.findByUserSeqAndProjectId(anyString(), anyString())).thenReturn(Optional.empty());

            com.ddiring.backend_asset.entitiy.Bank buyerBank = com.ddiring.backend_asset.entitiy.Bank.builder()
                    .userSeq(buyerUserSeq).role("USER").bankNumber("110-" + i).deposit(0).build();
            when(bankRepository.findByUserSeqAndRole(buyerUserSeq, "USER")).thenReturn(Optional.of(buyerBank));
            when(escrowClient.escrowWithdrawal(org.mockito.ArgumentMatchers.any()))
                    .thenReturn(org.springframework.http.ResponseEntity.ok("OK"));

            TradeInfoResponseDto tradeInfo = TradeInfoResponseDto.builder()
                    .tradeId(tradeId)
                    .projectId(projectId)
                    .price(price)
                    .tokenQuantity(tokenQuantity)
                    .buyerUserSeq(buyerUserSeq)
                    .sellerUserSeq(sellerUserSeq)
                    .status("PROGRESS")
                    .build();
            when(marketClient.getTradeInfo(tradeId)).thenReturn(ApiResponseDto.createOk(tradeInfo));

            TokenService tokenService = new TokenService(tokenRepository, escrowRepository, messagingTemplate);
            BankService bankService = new BankService(bankRepository, historyRepository, tokenRepository, escrowRepository, escrowClient);

            KafkaTradeEventsListener listener = new KafkaTradeEventsListener(
                    objectMapper, tokenService, walletRepository, bankService,
                    escrowRepository, marketClient, escrowClient, kafkaMessageProducer
            );

            TradeFailedEvent event = TradeFailedEvent.of(projectId, tradeId, buyerAddress, sellerAddress,
                    (long) tokenQuantity, "PAYMENT_TIMEOUT", "결제 타임아웃 시뮬레이션 #" + i);

            try {
                listener.handleTradeFailed(event);

                // 상태 복원 검증: (1) 판매자 토큰 원복, (2) 구매자 결제금 환불
                org.mockito.ArgumentCaptor<com.ddiring.backend_asset.entitiy.Token> tokenCaptor =
                        org.mockito.ArgumentCaptor.forClass(com.ddiring.backend_asset.entitiy.Token.class);
                org.mockito.Mockito.verify(tokenRepository).save(tokenCaptor.capture());
                int restoredTokenAmount = tokenCaptor.getValue().getAmount();
                int restoredDeposit = buyerBank.getDeposit();

                // 구매 시 setBuyPrice가 거래 금액 + 3% 수수료를 차감하므로, 환불도 수수료 포함 금액이어야 한다.
                int expectedRefund = (int) (price + (price * 0.03));
                org.mockito.ArgumentCaptor<com.ddiring.backend_asset.api.escrow.EscrowDto> escrowCaptor =
                        org.mockito.ArgumentCaptor.forClass(com.ddiring.backend_asset.api.escrow.EscrowDto.class);
                org.mockito.Mockito.verify(escrowClient).escrowWithdrawal(escrowCaptor.capture());
                int escrowWithdrawn = escrowCaptor.getValue().getAmount();

                boolean tokenOk = restoredTokenAmount == tokenQuantity;
                boolean depositOk = restoredDeposit == expectedRefund;
                boolean escrowOk = escrowWithdrawn == expectedRefund;
                if (!tokenOk || !depositOk || !escrowOk) {
                    throw new AssertionError("복원 값 불일치: token=" + restoredTokenAmount + "(기대 " + tokenQuantity
                            + "), deposit=" + restoredDeposit + "(기대 " + expectedRefund
                            + "), escrow인출=" + escrowWithdrawn + "(기대 " + expectedRefund + ")");
                }

                restored++;
                if (i <= 3) {
                    System.out.println("[복원 성공] tradeId=" + tradeId + " sellerToken=+" + restoredTokenAmount
                            + " buyerDeposit=+" + restoredDeposit + " (거래금액 " + price + " + 수수료 "
                            + (expectedRefund - price) + ")");
                }
            } catch (Exception | AssertionError e) {
                failed++;
                if (failed <= 3) {
                    System.out.println("[복원 실패] tradeId=" + tradeId + " -> " + e.getClass().getSimpleName()
                            + ": " + rootCauseMessage(e));
                }
            }
        }

        System.out.println("\n===== 보상 트랜잭션 실패 시나리오 테스트 결과 =====");
        System.out.println("총 시도: " + totalTrials + "건, 복원 성공: " + restored + "건, 복원 실패: " + failed + "건");
        org.junit.jupiter.api.Assertions.assertEquals(0, failed, "보상 트랜잭션 복원 실패 건수");
    }

    private String rootCauseMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) {
            cur = cur.getCause();
        }
        return cur.getClass().getSimpleName() + (cur.getMessage() != null ? (": " + cur.getMessage()) : "");
    }
}
