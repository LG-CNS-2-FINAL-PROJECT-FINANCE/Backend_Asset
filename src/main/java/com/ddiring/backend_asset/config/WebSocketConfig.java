package com.ddiring.backend_asset.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 웹소켓 연결을 시작할 엔드포인트
        // 예: http://localhost:8082/ws
        registry.addEndpoint("192.168.0.222:8080/wallet-token/search")
                .setAllowedOriginPatterns("*") // 모든 출처에서의 연결 허용 (CORS)
                .withSockJS(); // SockJS는 웹소켓을 지원하지 않는 브라우저를 위한 대체 옵션
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}