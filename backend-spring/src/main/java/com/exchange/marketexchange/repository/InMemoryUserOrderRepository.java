package com.exchange.marketexchange.repository;

import com.exchange.marketexchange.model.Order;
import com.exchange.marketexchange.model.OrderType;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap; // Corrected import
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Repository
public class InMemoryUserOrderRepository {

    // Structure: tradingPair -> { price -> List<Order> }
    // For bids, prices are in descending order. For asks, prices are in ascending order.
    private final Map<String, NavigableMap<BigDecimal, List<Order>>> bids = new ConcurrentHashMap<>();
    private final Map<String, NavigableMap<BigDecimal, List<Order>>> asks = new ConcurrentHashMap<>();
    private final Map<String, Order> ordersById = new ConcurrentHashMap<>(); // For quick lookup/removal by ID

    public void addOrder(Order order) {
        if (order == null || order.getTradingPair() == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Order, trading pair, and order ID cannot be null.");
        }
        ordersById.put(order.getOrderId(), order);

        NavigableMap<BigDecimal, List<Order>> orderBookSide;
        if (order.getType() == OrderType.BUY) {
            orderBookSide = bids.computeIfAbsent(order.getTradingPair(),
                    k -> new TreeMap<>(Collections.reverseOrder())); // Bids: highest price first
        } else {
            orderBookSide = asks.computeIfAbsent(order.getTradingPair(),
                    k -> new TreeMap<>()); // Asks: lowest price first
        }

        synchronized (orderBookSide) {
            List<Order> ordersAtPrice = orderBookSide.computeIfAbsent(order.getPrice(), k -> new CopyOnWriteArrayList<>());
            ordersAtPrice.add(order);
        }
    }

    public boolean removeOrder(String orderId) {
        Order orderToRemove = ordersById.remove(orderId);
        if (orderToRemove == null) {
            return false;
        }

        NavigableMap<BigDecimal, List<Order>> orderBookSide;
        if (orderToRemove.getType() == OrderType.BUY) {
            orderBookSide = bids.get(orderToRemove.getTradingPair());
        } else {
            orderBookSide = asks.get(orderToRemove.getTradingPair());
        }

        if (orderBookSide != null) {
            synchronized (orderBookSide) {
                List<Order> ordersAtPrice = orderBookSide.get(orderToRemove.getPrice());
                if (ordersAtPrice != null) {
                    boolean removed = ordersAtPrice.removeIf(o -> o.getOrderId().equals(orderId));
                    if (ordersAtPrice.isEmpty()) {
                        orderBookSide.remove(orderToRemove.getPrice());
                    }
                    if (orderBookSide.isEmpty()){
                        (orderToRemove.getType() == OrderType.BUY ? bids : asks).remove(orderToRemove.getTradingPair());
                    }
                    return removed;
                }
            }
        }
        return false; // Should not happen if ordersById was consistent
    }

    public List<Order> getBidsList(String tradingPair) {
        NavigableMap<BigDecimal, List<Order>> pairBids = bids.get(tradingPair);
        if (pairBids == null) {
            return Collections.emptyList();
        }
        synchronized (pairBids) {
            return pairBids.values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
    }

    public List<Order> getAsksList(String tradingPair) {
        NavigableMap<BigDecimal, List<Order>> pairAsks = asks.get(tradingPair);
        if (pairAsks == null) {
            return Collections.emptyList();
        }
        synchronized (pairAsks) {
            return pairAsks.values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
    }
    
    public NavigableMap<BigDecimal, List<Order>> getBidsMap(String tradingPair) {
        return bids.getOrDefault(tradingPair, new TreeMap<>(Collections.reverseOrder()));
    }

    public NavigableMap<BigDecimal, List<Order>> getAsksMap(String tradingPair) {
        return asks.getOrDefault(tradingPair, new TreeMap<>());
    }


    public List<Order> getOrdersByUserId(String userId) {
        return ordersById.values().stream()
                .filter(order -> userId.equals(order.getUserId()))
                .collect(Collectors.toList());
    }

    public Order getOrderById(String orderId) {
        return ordersById.get(orderId);
    }
    
    // For testing or clearing purposes
    public void clearAllOrders() {
        bids.clear();
        asks.clear();
        ordersById.clear();
    }
}
