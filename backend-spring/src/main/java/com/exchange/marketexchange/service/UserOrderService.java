package com.exchange.marketexchange.service;

import com.exchange.marketexchange.model.Order;
import com.exchange.marketexchange.model.OrderSource;
import com.exchange.marketexchange.repository.InMemoryUserOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; // Added import for @Lazy
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserOrderService {

    private final InMemoryUserOrderRepository orderRepository;
    private CombinedOrderBookService combinedOrderBookService; // Setter injection

    @Autowired
    public UserOrderService(InMemoryUserOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Setter for CombinedOrderBookService to break circular dependency
    @Autowired
    public void setCombinedOrderBookService(@Lazy CombinedOrderBookService combinedOrderBookService) { // Added @Lazy
        this.combinedOrderBookService = combinedOrderBookService;
    }

    public Order createOrder(Order orderDetails, String userId) {
        if (orderDetails.getPrice() == null || orderDetails.getQuantity() == null || 
            orderDetails.getType() == null || orderDetails.getTradingPair() == null) {
            throw new IllegalArgumentException("Price, quantity, type, and trading pair must be provided for an order.");
        }

        Order newOrder = new Order();
        newOrder.setOrderId(UUID.randomUUID().toString());
        newOrder.setUserId(userId);
        newOrder.setType(orderDetails.getType());
        newOrder.setPrice(orderDetails.getPrice());
        newOrder.setQuantity(orderDetails.getQuantity());
        newOrder.setTimestamp(LocalDateTime.now());
        newOrder.setSource(OrderSource.USER);
        newOrder.setTradingPair(orderDetails.getTradingPair());

        orderRepository.addOrder(newOrder);
        if (combinedOrderBookService != null) {
            combinedOrderBookService.publishCombinedOrderBookUpdate();
        }
        return newOrder;
    }

    public boolean cancelOrder(String orderId, String userId) {
        Order orderToCancel = orderRepository.getOrderById(orderId);
        if (orderToCancel == null) {
            // Order not found
            return false;
        }
        if (!userId.equals(orderToCancel.getUserId())) {
            // User does not own this order
            // In a real app, might throw a specific security exception
            return false; 
        }
        if (orderToCancel.getSource() != OrderSource.USER) {
            // Cannot cancel non-user orders
            return false;
        }

        boolean removed = orderRepository.removeOrder(orderId);
        if (removed && combinedOrderBookService != null) {
            combinedOrderBookService.publishCombinedOrderBookUpdate();
        }
        return removed;
    }

    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.getOrdersByUserId(userId);
    }

    public Order getOrderById(String orderId) {
        return orderRepository.getOrderById(orderId);
    }
}
