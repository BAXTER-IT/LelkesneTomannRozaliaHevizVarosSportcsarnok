package com.exchange.marketexchange.handler;

import com.exchange.marketexchange.model.CombinedOrderBook;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class OrderBookWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(OrderBookWebSocketHandler.class);
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // For now, only allow authenticated users to connect if Spring Security principal is available
        // In a real scenario, we'd check session.getPrincipal()
        // For simplicity in this phase, we allow all connections that reach here.
        // SecurityConfig already permits /ws/**
        
        logger.info("WebSocket connection established: {}", session.getId());
        sessions.add(session);
        // Optionally, send an initial message or current state
        // For example, send a welcome message or the current order book if available
        // session.sendMessage(new TextMessage("Welcome! Connected to Order Book."));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages from clients if needed
        // For an order book, clients usually just receive data, but they might send subscription requests
        String payload = message.getPayload();
        logger.info("Received WebSocket message from {}: {}", session.getId(), payload);
        // Example: session.sendMessage(new TextMessage("Echo: " + payload));
        // We might parse client messages if they subscribe to specific trading pairs, e.g.
        // Map<String, String> clientMessage = objectMapper.readValue(payload, Map.class);
        // if ("subscribe".equals(clientMessage.get("action"))) {
        //     String tradingPair = clientMessage.get("tradingPair");
        //     // Store subscription preference for the session
        // }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: {} with status {}", session.getId(), status);
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
        sessions.remove(session);
    }

    public void broadcastOrderBookUpdate(CombinedOrderBook orderBook) {
        if (orderBook == null) {
            logger.warn("Attempted to broadcast a null order book.");
            return;
        }
        try {
            String messagePayload = objectMapper.writeValueAsString(orderBook);
            TextMessage message = new TextMessage(messagePayload);
            for (WebSocketSession session : sessions) {
                // Here we could add logic to send only if the session is subscribed to orderBook.getTradingPair()
                if (session.isOpen()) {
                    try {
                        synchronized(session) { // Ensure thread-safe sending per session
                           session.sendMessage(message);
                        }
                    } catch (IOException e) {
                        logger.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
                        // Consider removing session if send fails repeatedly
                    }
                } else {
                    // Clean up closed sessions that might not have been removed by afterConnectionClosed
                    sessions.remove(session);
                }
            }
        } catch (IOException e) {
            logger.error("Error serializing order book for broadcast: {}", e.getMessage());
        }
    }
}
