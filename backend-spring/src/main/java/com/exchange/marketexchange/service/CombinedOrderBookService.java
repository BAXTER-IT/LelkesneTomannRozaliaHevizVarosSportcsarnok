package com.exchange.marketexchange.service;

import com.exchange.marketexchange.handler.OrderBookWebSocketHandler;
import com.exchange.marketexchange.model.CombinedOrderBook;
import com.exchange.marketexchange.model.Order;
import com.exchange.marketexchange.model.OrderBookEntry;
import com.exchange.marketexchange.model.OrderType;
import com.exchange.marketexchange.repository.InMemoryUserOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set; // Added
import java.util.TreeSet; // Added
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.exchange.marketexchange.model.OrderSource; // Added

@Service
public class CombinedOrderBookService {

    private static final Logger logger = LoggerFactory.getLogger(CombinedOrderBookService.class);
    private static final int DEPTH_LIMIT = 5; // Show top 5 bids and asks

    private final InMemoryUserOrderRepository userOrderRepository;
    private final BinanceDataService binanceDataService;
    private final OrderBookWebSocketHandler webSocketHandler;
    private UserOrderService userOrderService; // Changed to allow setter injection


    @Autowired
    public CombinedOrderBookService(InMemoryUserOrderRepository userOrderRepository,
                                    BinanceDataService binanceDataService,
                                    OrderBookWebSocketHandler webSocketHandler) {
        this.userOrderRepository = userOrderRepository;
        this.binanceDataService = binanceDataService;
        this.webSocketHandler = webSocketHandler;
        // UserOrderService will be set via setter
    }

    @Autowired
    public void setUserOrderService(UserOrderService userOrderService) {
        this.userOrderService = userOrderService;
    }

    @PostConstruct
    private void init() {
        // Register a callback with BinanceDataService so it can trigger updates
        this.binanceDataService.setOnUpdateCallback(this::publishCombinedOrderBookUpdate);
        // UserOrderService is now setter injected, so it will call this service's method directly.
    }

    public void publishCombinedOrderBookUpdate() {
        // For now, let's assume a default trading pair. This should be made dynamic later.
        String defaultTradingPair = "BTCUSDT"; 
        CombinedOrderBook combinedOrderBook = getCombinedOrderBook(defaultTradingPair);
        webSocketHandler.broadcastOrderBookUpdate(combinedOrderBook);
        logger.debug("Published combined order book update for {}", defaultTradingPair);
    }

    public CombinedOrderBook getCombinedOrderBook(String tradingPair) {
        // 1. Get user orders
        NavigableMap<BigDecimal, List<Order>> userBidsMap = userOrderRepository.getBidsMap(tradingPair);
        NavigableMap<BigDecimal, List<Order>> userAsksMap = userOrderRepository.getAsksMap(tradingPair);

        // 2. Get Binance orders
        List<OrderBookEntry> binanceBidsList = binanceDataService.getBinanceBids();
        List<OrderBookEntry> binanceAsksList = binanceDataService.getBinanceAsks();

        // 3. Aggregate user and Binance orders separately
        Map<BigDecimal, BigDecimal> userAggregatedBids = new TreeMap<>(Collections.reverseOrder());
        Map<BigDecimal, BigDecimal> binanceAggregatedBids = new TreeMap<>(Collections.reverseOrder());
        processOrdersForAggregation(userAggregatedBids, userBidsMap, OrderType.BUY);
        processExternalEntriesForAggregation(binanceAggregatedBids, binanceBidsList);

        Map<BigDecimal, BigDecimal> userAggregatedAsks = new TreeMap<>();
        Map<BigDecimal, BigDecimal> binanceAggregatedAsks = new TreeMap<>();
        processOrdersForAggregation(userAggregatedAsks, userAsksMap, OrderType.SELL);
        processExternalEntriesForAggregation(binanceAggregatedAsks, binanceAsksList);

        // 4. Combine aggregated data, set source, and limit depth
        Set<BigDecimal> allBidPrices = new TreeSet<>(Collections.reverseOrder());
        allBidPrices.addAll(userAggregatedBids.keySet());
        allBidPrices.addAll(binanceAggregatedBids.keySet());

        List<OrderBookEntry> topBids = allBidPrices.stream()
                .map(price -> {
                    BigDecimal userQty = userAggregatedBids.getOrDefault(price, BigDecimal.ZERO);
                    BigDecimal binanceQty = binanceAggregatedBids.getOrDefault(price, BigDecimal.ZERO);
                    BigDecimal totalQty = userQty.add(binanceQty);
                    OrderSource source = userQty.compareTo(BigDecimal.ZERO) > 0 ? OrderSource.USER : OrderSource.BINANCE;
                    return new OrderBookEntry(price, totalQty, source);
                })
                .limit(DEPTH_LIMIT)
                .collect(Collectors.toList());

        Set<BigDecimal> allAskPrices = new TreeSet<>(); // Natural order for asks
        allAskPrices.addAll(userAggregatedAsks.keySet());
        allAskPrices.addAll(binanceAggregatedAsks.keySet());

        List<OrderBookEntry> topAsks = allAskPrices.stream()
                .map(price -> {
                    BigDecimal userQty = userAggregatedAsks.getOrDefault(price, BigDecimal.ZERO);
                    BigDecimal binanceQty = binanceAggregatedAsks.getOrDefault(price, BigDecimal.ZERO);
                    BigDecimal totalQty = userQty.add(binanceQty);
                    OrderSource source = userQty.compareTo(BigDecimal.ZERO) > 0 ? OrderSource.USER : OrderSource.BINANCE;
                    return new OrderBookEntry(price, totalQty, source);
                })
                .limit(DEPTH_LIMIT)
                .collect(Collectors.toList());
        
        return new CombinedOrderBook(tradingPair, topBids, topAsks, System.currentTimeMillis());
    }

    private void processOrdersForAggregation(Map<BigDecimal, BigDecimal> aggregatedMap,
                                             NavigableMap<BigDecimal, List<Order>> ordersMap,
                                             OrderType type) {
        // The NavigableMap from repository is already sorted correctly.
        for (Map.Entry<BigDecimal, List<Order>> entry : ordersMap.entrySet()) {
            BigDecimal price = entry.getKey();
            BigDecimal quantityAtPrice = entry.getValue().stream()
                                            .map(Order::getQuantity)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
            aggregatedMap.merge(price, quantityAtPrice, BigDecimal::add);
        }
    }
    
    private void processExternalEntriesForAggregation(Map<BigDecimal, BigDecimal> aggregatedMap,
                                                      List<OrderBookEntry> externalEntries) {
        if (externalEntries == null) return; // Add null check for safety
        for (OrderBookEntry entry : externalEntries) {
            if (entry.getPrice() != null && entry.getTotalQuantity() != null) { // Add null checks for entry fields
                aggregatedMap.merge(entry.getPrice(), entry.getTotalQuantity(), BigDecimal::add);
            }
        }
    }
}
