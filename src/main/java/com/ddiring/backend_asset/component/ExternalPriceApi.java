package com.ddiring.backend_asset.component;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.util.Map;

@Component
public class ExternalPriceApi {
    private final WebClient webClient = WebClient.builder().baseUrl("https://api.coingecko.com/api/v3").build();

    /**
     * 주어진 이더리움 토큰 컨트랙트 주소에 대한 KRW 가격을 CoinGecko API를 통해 조회합니다.
     *
     * @param tokenContractAddress 조회할 토큰의 이더리움 컨트랙트 주소 (예: "0x...")
     * @return 토큰의 KRW 가격 (BigDecimal), 조회 실패 시 BigDecimal.ZERO 반환
     */
    public BigDecimal getTokenPriceInKRW(String tokenContractAddress) {
        // 발급받으신 CoinGecko API 키를 여기에 입력해주세요.
        // 유료 플랜을 사용하시는 경우에 필요합니다.
        // 무료 플랜은 'x_cg_demo_api_key' 없이도 작동할 수 있습니다.
        String apiKey = "CG-YdYnWWmT6rTzcus9urVmskH9"; // <-- 여기에 실제 API 키를 넣어주세요!

        // CoinGecko API 엔드포인트: simple/token_price
        // 플랫폼 ID는 이더리움의 경우 "ethereum"입니다.
        // vs_currencies는 조회하려는 통화 (예: "krw")입니다.
        // API 키는 'x_cg_demo_api_key' 쿼리 파라미터로 전달됩니다.
        String uri = String.format("/simple/token_price/ethereum?contract_addresses=%s&vs_currencies=krw&x_cg_demo_api_key=%s", tokenContractAddress, apiKey);

        try {
            // WebClient를 사용하여 GET 요청을 보냅니다.
            // 응답은 Map<String, Map<String, BigDecimal>> 형태로 파싱됩니다.
            // 예시 응답: {"0xcontractaddress": {"krw": 1500.50}}
            Mono<Map> responseMono = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(Map.class);

            // 응답을 동기적으로 블록하고 파싱합니다.
            // 실제 프로덕션 환경에서는 Mono를 반환하여 비동기적으로 처리하는 것이 좋습니다.
            Map<String, Map<String, BigDecimal>> response = responseMono.block();

            if (response != null && response.containsKey(tokenContractAddress)) {
                Map<String, BigDecimal> prices = response.get(tokenContractAddress);
                if (prices != null && prices.containsKey("krw")) {
                    return prices.get("krw");
                }
            }
            System.out.println("CoinGecko: 토큰 " + tokenContractAddress + "의 가격 정보를 찾을 수 없습니다.");
            return BigDecimal.ZERO;

        } catch (Exception e) {
            System.err.println("CoinGecko API 호출 중 오류 발생 (컨트랙트 주소: " + tokenContractAddress + "): " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}
