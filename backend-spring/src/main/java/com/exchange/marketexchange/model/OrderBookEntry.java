package com.exchange.marketexchange.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookEntry {
    private BigDecimal price;
    private BigDecimal totalQuantity;
}
