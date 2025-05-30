package com.exchange.marketexchange.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombinedOrderBook {
    private String tradingPair;
    private List<OrderBookEntry> bids; // Top 5 bids
    private List<OrderBookEntry> asks; // Top 5 asks
    private long timestamp; // Unix timestamp for when this book was generated
}
