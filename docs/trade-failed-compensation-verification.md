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

## 3차 검증 — 구매 수수료(3%) 미환불 발견 및 수정 (1,000건)

2차 검증은 "판매자 토큰 + 거래 금액" 기준으로만 맞았다. 구매 시 `BankService.setBuyPrice()`는 구매자 은행 잔액에서 **거래 금액 + 3% 수수료**를 차감하는데, 보상 트랜잭션은 `refundPrice`에 거래 금액만 넘겨 **구매자가 낸 3% 수수료는 환불되지 않았다.**

검증 방식을 보강했다. (1) 건수를 10건에서 1,000건으로 늘리고, (2) 기대 환불액을 `거래 금액 + 3% 수수료`로 바꾸고, (3) 구매자 예치금뿐 아니라 에스크로 인출 요청 금액(`escrowWithdrawal` 인자)까지 ArgumentCaptor로 확인했다.

**수정 전: 1,000건 중 0건 복원 성공 (1,000건 전부 실패)** — 예) 거래 금액 101,000원 거래에서 환불 101,000원, 기대 104,030원(수수료 3,030원 누락).

**수정**: `handleTradeFailed()`에서 환불 금액을 `setBuyPrice`가 차감한 금액과 동일하게 계산해 전달.

```diff
+ int refundWithFee = (int) (tradeInfo.getPrice() + (tradeInfo.getPrice() * 0.03));
  MarketRefundDto marketRefundDto = MarketRefundDto.builder()
-         .refundPrice(tradeInfo.getPrice())
+         .refundPrice(refundWithFee)
          .ordersId(payload.getTradeId().intValue())
          .projectId(tradeInfo.getProjectId())
          .orderType(2)
          .build();
```

`setRefundToken`의 `orderType=2` 분기는 REST 환불(`AssetController`)에서도 쓰이므로 분기 로직은 건드리지 않고, 보상 트랜잭션이 넘기는 금액만 바꿨다.

**수정 후: 1,000건 중 1,000건 복원 성공 (1,000/1,000)** — 판매자 토큰 원복, 구매자 예치금 환불(거래 금액 + 수수료), 에스크로 인출액 세 가지 모두 기대값과 일치. 예) tradeId 1001: 토큰 +11, 예치금 +104,030원(101,000 + 수수료 3,030), tradeId 1002: 토큰 +12, 예치금 +105,060원(102,000 + 수수료 3,060).

가정: 보상 대상 거래는 2차 시장 거래(`setBuyPrice`의 수수료 차감 분기)로 보고 수수료를 포함해 환불했다.

## 결론 (면접 답변용)

> "보상 트랜잭션을 실측 검증하는 과정에서 버그를 두 번 찾았습니다. 첫 번째는 구매자 환불 시 `MarketRefundDto`의 `orderType`이 비어 있어 매번 NullPointerException이 나던 문제였고, 10건 중 0건만 복원됐습니다. 에스크로 인출 방식으로 지정해 고쳤더니 10건 모두 복원됐습니다. 두 번째는 그 검증이 거래 금액만 비교하고 있어서 놓친 문제였습니다. 구매 시 3% 수수료까지 차감되는데 환불에는 수수료가 빠져 있었습니다. 기대값을 수수료 포함 금액으로 바꾸고 건수를 1,000건으로 늘려 확인하니 수정 전에는 1,000건 전부 수수료가 누락됐고, 환불액을 구매 시 차감액과 같게 맞춘 뒤에는 1,000건 모두 토큰, 예치금, 에스크로 인출액이 정확히 복원됐습니다. '복원됐다'를 검증할 때 무엇을 기준으로 삼는지가 결과를 바꾼다는 걸 배웠습니다."
