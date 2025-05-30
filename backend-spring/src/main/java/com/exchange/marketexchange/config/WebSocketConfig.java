package com.exchange.marketexchange.config;

import com.exchange.marketexchange.handler.OrderBookWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OrderBookWebSocketHandler orderBookWebSocketHandler;

    public WebSocketConfig(OrderBookWebSocketHandler orderBookWebSocketHandler) {
        this.orderBookWebSocketHandler = orderBookWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderBookWebSocketHandler, "/ws/market") // Our main WebSocket endpoint
                .setAllowedOrigins("*"); // Allow all origins for now, can be restricted later
    }
}
