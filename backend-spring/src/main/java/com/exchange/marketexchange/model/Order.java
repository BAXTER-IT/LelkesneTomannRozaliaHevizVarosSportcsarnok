package com.exchange.marketexchange.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String userId; // Nullable if order is from Binance
    private OrderType type;
    private BigDecimal price;
    private BigDecimal quantity;
    private LocalDateTime timestamp;
    private OrderSource source;
    private String tradingPair; // e.g., "BTCUSDT"
}
