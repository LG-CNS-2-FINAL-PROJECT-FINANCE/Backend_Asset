# TRADE.FAILED 보상 트랜잭션 검증 결과

## 배경

거래(Trade) 처리 실패 시 `KafkaTradeEventsListener.handleTradeFailed()`가 판매자 토큰과 구매자 결제금을 원상복구하는 보상 트랜잭션을 수행한다. 이 로직이 실제로 동작하는지 Mockito 기반 단위 테스트([KafkaTradeEventsListenerCompensationTest.java](../src/test/java/com/ddiring/backend_asset/event/consumer/KafkaTradeEventsListenerCompensationTest.java))로 N=10건 반복 검증했다.

## 1차 검증 — 버그 발견

`handleTradeFailed()`가 구매자 환불용 `MarketRefundDto`를 만들 때 `orderType`, `refundAmount`를 채우지 않아 `null`로 남았다. `BankService.setRefundToken()`의 첫 줄 `if (marketRefundDto.getOrderType() == 0)`이 `Integer` 언박싱 과정에서 **매 호출마다 NullPointerException**을 던졌다.

**결과: 10건 중 0건 복원 성공 (0/10)**

```
java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()"
because the return value of "MarketRefundDto.getOrderType()" is null
    at BankService.setRefundToken(BankService.java:282)
    at KafkaTradeEventsListener.handleTradeFailed(KafkaTradeEventsListener.java:157)
```

## 원인

`setRefundToken()`은 `orderType`에 따라 3가지 환불 방식을 분기한다.

| orderType | 동작 |
|---|---|
| 0 | 토큰 수량 환불 (`refundAmount`만큼 토큰 증가) |
| 1 | 에스크로 예치 + 수수료 3% 가산 (`escrowDeposit`) |
| 2 | 에스크로 인출 → 은행 잔액 반환 (`escrowWithdrawal`, `refundPrice`만 사용) |

거래 실패 시 구매자의 결제금은 에스크로에 보관돼 있던 상태이므로, 이를 인출해 구매자 은행 잔액으로 돌려주는 **orderType=2**가 의미상 맞다. 그런데 원래 코드는 `orderType`을 아예 설정하지 않아 세 분기 중 어디로도 가지 못하고 NPE로 끝나고 있었다.

## 수정

`KafkaTradeEventsListener.handleTradeFailed()`에서 `MarketRefundDto` 빌더에 `.orderType(2)` 한 줄 추가.

```diff
 MarketRefundDto marketRefundDto = MarketRefundDto.builder()
         .refundPrice(tradeInfo.getPrice())
         .ordersId(payload.getTradeId().intValue())
         .projectId(tradeInfo.getProjectId())
+        .orderType(2)
         .build();
```

## 2차 검증 — 수정 후 재실행

같은 10건 시나리오로 재실행. 각 건마다 (1) 판매자 토큰 수량 원복, (2) 구매자 은행 예치금 환불 두 가지를 모두 검증(ArgumentCaptor로 실제 저장된 값 확인).

**결과: 10건 중 10건 복원 성공 (10/10)**

| tradeId | 판매자 토큰 원복 | 구매자 예치금 환불 |
|---|---|---|
| 1001 | +11 | +101,000원 |
| ... | ... | ... |
| 1010 | +20 | +110,000원 |

모든 건에서 예상 값(토큰 수량, 결제 가격)과 실제 복원된 값이 정확히 일치.

## 결론 (면접 답변용)

> "보상 트랜잭션 로직을 실측 검증하는 과정에서, 구매자 환불 시 `MarketRefundDto`의 `orderType`이 설정되지 않아 매번 NullPointerException으로 실패하는 버그를 발견했습니다. 원본 코드로는 10건 중 0건만 복원됐습니다. 원인을 분석해 에스크로 인출 방식(orderType=2)으로 명시적으로 지정하도록 수정했고, 재검증 결과 10건 중 10건 모두 판매자 토큰과 구매자 결제금이 정확히 복원되는 것을 확인했습니다. 이 과정에서 '보상 트랜잭션이 있다'는 것과 '보상 트랜잭션이 실제로 동작한다'는 것은 다르다는 걸 실감했고, 이후로는 실패 경로도 반드시 테스트로 검증하는 습관을 들이게 됐습니다."
