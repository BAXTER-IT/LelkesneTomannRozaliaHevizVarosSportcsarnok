package com.exchange.marketexchange.service;

import com.exchange.marketexchange.model.OrderBookEntry;
import com.exchange.marketexchange.model.dto.BinanceDepthStreamPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.exchange.marketexchange.model.OrderSource; // Added

@Service
public class BinanceDataService {

    private static final Logger logger = LoggerFactory.getLogger(BinanceDataService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebSocketClient webSocketClient;

    // Store the latest top 5 bids and asks from Binance
    private final List<OrderBookEntry> binanceBids = new CopyOnWriteArrayList<>();
    private final List<OrderBookEntry> binanceAsks = new CopyOnWriteArrayList<>();

    // Will be injected from CombinedOrderBookService to trigger updates
    private Runnable onUpdateCallback; 

    @Value("${binance.websocket.url:wss://stream.binance.com:9443/ws/btcusdt@depth5@100ms}")
    private String binanceWsUrl;

    public void setOnUpdateCallback(Runnable onUpdateCallback) {
        this.onUpdateCallback = onUpdateCallback;
    }

    @PostConstruct
    public void connect() {
        try {
            webSocketClient = new WebSocketClient(new URI(binanceWsUrl)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    logger.info("Connected to Binance WebSocket: {}", binanceWsUrl);
                    // We could send a subscription message here if Binance required it for this stream,
                    // but for @depthN streams, it's usually not needed.
                }

                @Override
                public void onMessage(String message) {
                    logger.debug("Received from Binance: {}", message);
                    try {
                        // The depth stream payload is nested under a "stream" and "data" field for combined streams
                        // For single streams like "btcusdt@depth5", it might be the direct payload.
                        // Let's assume direct payload for @depth5 as per typical Binance behavior.
                        JsonNode rootNode = objectMapper.readTree(message);
                        BinanceDepthStreamPayload payload;

                        // Check if it's a combined stream format or direct payload
                        if (rootNode.has("stream") && rootNode.has("data")) {
                             payload = objectMapper.treeToValue(rootNode.get("data"), BinanceDepthStreamPayload.class);
                        } else {
                             payload = objectMapper.readValue(message, BinanceDepthStreamPayload.class);
                        }
                        
                        updateLocalOrderBook(payload);
                        if (onUpdateCallback != null) {
                            onUpdateCallback.run();
                        }
                    } catch (JsonProcessingException e) {
                        logger.error("Error parsing Binance message: {}", e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logger.warn("Disconnected from Binance WebSocket: code={}, reason={}, remote={}", code, reason, remote);
                    // Implement reconnection logic if needed
                }

                @Override
                public void onError(Exception ex) {
                    logger.error("Error with Binance WebSocket connection: {}", ex.getMessage());
                }
            };
            logger.info("Attempting to connect to Binance WebSocket at {}", binanceWsUrl);
            webSocketClient.connect(); // Asynchronous connect
        } catch (URISyntaxException e) {
            logger.error("Invalid Binance WebSocket URI: {}", binanceWsUrl, e);
        }
    }

    private void updateLocalOrderBook(BinanceDepthStreamPayload payload) {
        if (payload.getBids() != null) {
            binanceBids.clear();
            binanceBids.addAll(payload.getBids().stream()
                    .map(entry -> new OrderBookEntry(new BigDecimal(entry.get(0)), new BigDecimal(entry.get(1)), OrderSource.BINANCE))
                    .collect(Collectors.toList()));
        }
        if (payload.getAsks() != null) {
            binanceAsks.clear();
            binanceAsks.addAll(payload.getAsks().stream()
                    .map(entry -> new OrderBookEntry(new BigDecimal(entry.get(0)), new BigDecimal(entry.get(1)), OrderSource.BINANCE))
                    .collect(Collectors.toList()));
        }
        logger.debug("Updated local Binance order book. Bids: {}, Asks: {}", binanceBids.size(), binanceAsks.size());
    }

    public List<OrderBookEntry> getBinanceBids() {
        return Collections.unmodifiableList(binanceBids);
    }

    public List<OrderBookEntry> getBinanceAsks() {
        return Collections.unmodifiableList(binanceAsks);
    }

    @PreDestroy
    public void disconnect() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            logger.info("Disconnecting from Binance WebSocket.");
            webSocketClient.close();
        }
    }
}
