package com.exchange.marketexchange.service;

import com.exchange.marketexchange.model.Order;
import com.exchange.marketexchange.model.OrderSource;
import com.exchange.marketexchange.model.User; // Added import for User
import com.exchange.marketexchange.repository.OrderRepository; // Changed import
import com.exchange.marketexchange.repository.UserRepository; // Added import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // For DB operations

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserOrderService {

    private final OrderRepository orderRepository; // Changed from InMemoryUserOrderRepository
    private final UserRepository userRepository; // Added UserRepository
    private CombinedOrderBookService combinedOrderBookService; // Setter injection

    @Autowired
    public UserOrderService(OrderRepository orderRepository, UserRepository userRepository) { // Updated constructor
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    // Setter for CombinedOrderBookService to break circular dependency
    @Autowired
    public void setCombinedOrderBookService(@Lazy CombinedOrderBookService combinedOrderBookService) {
        this.combinedOrderBookService = combinedOrderBookService;
    }

    @Transactional // Make it a transactional method
    public Order createOrder(Order orderDetails, String username) { // username instead of userId
        if (orderDetails.getPrice() == null || orderDetails.getQuantity() == null ||
            orderDetails.getType() == null || orderDetails.getTradingPair() == null) {
            throw new IllegalArgumentException("Price, quantity, type, and trading pair must be provided for an order.");
        }

        // Find or create the User entity
        User user = userRepository.findByUsername(username)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setUserId(UUID.randomUUID().toString()); // Set the ID for the new user
                newUser.setUsername(username);
                // passwordHash is not set here as auth is via InMemoryUserDetailsManager
                return userRepository.save(newUser);
            });

        // Create a new Order entity using the constructor that takes a User object
        Order newOrder = new Order(
            user,
            orderDetails.getType(),
            orderDetails.getPrice(),
            orderDetails.getQuantity(),
            orderDetails.getTradingPair(),
            OrderSource.USER // Source is always USER for orders created through this service
        );
        // orderId is set within the Order constructor

        Order savedOrder = orderRepository.save(newOrder);

        if (combinedOrderBookService != null) {
            combinedOrderBookService.publishCombinedOrderBookUpdate();
        }
        return savedOrder;
    }

    @Transactional // Make it a transactional method
    public boolean cancelOrder(String orderId, String username) { // username instead of userId
        Optional<Order> orderToCancelOpt = orderRepository.findByOrderIdAndUser_Username(orderId, username);

        if (!orderToCancelOpt.isPresent()) {
            // Order not found or does not belong to the user
            return false;
        }

        Order orderToCancel = orderToCancelOpt.get();
        if (orderToCancel.getSource() != OrderSource.USER) {
            // Cannot cancel non-user orders (though findByOrderIdAndUser_Username should only return user orders)
            return false;
        }

        orderRepository.delete(orderToCancel);

        if (combinedOrderBookService != null) {
            combinedOrderBookService.publishCombinedOrderBookUpdate();
        }
        return true;
    }

    // Renamed from getOrdersByUserId to getOrdersByUsername for clarity
    @Transactional(readOnly = true) // readOnly transaction for queries
    public List<Order> getOrdersByUsername(String username) {
        return orderRepository.findByUser_UsernameOrderByTimestampDesc(username);
    }

    // This method might be for internal use or admin purposes if it fetches any order by its DB ID.
    // If it's meant for users, it should probably also be scoped by username.
    // For now, let's assume it's a general fetch by DB ID.
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long id) { // Changed to Long for DB ID
        return orderRepository.findById(id);
    }

    // If you need to fetch an order by its business orderId (UUID string) without user context:
    @Transactional(readOnly = true)
    public Optional<Order> getOrderByOrderId(String orderId) {
        // This would require a method in OrderRepository like: Optional<Order> findByOrderId(String orderId);
        // For now, we don't have such a generic method, only user-scoped.
        // If needed, it can be added to OrderRepository.
        // For user-specific fetch by orderId:
        // return orderRepository.findByOrderIdAndUser_Username(orderId, username); // but needs username
        return Optional.empty(); // Placeholder, implement if needed with a new repo method
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrderByOrderIdAndUsername(String orderId, String username) {
        return orderRepository.findByOrderIdAndUser_Username(orderId, username);
    }
     
    // To get all orders from USER source (which are persisted)
    // This is used by CombinedOrderBookService
    @Transactional(readOnly = true)
    public List<Order> getAllUserPersistedOrders() {
        // We need a method in OrderRepository to fetch all orders with source USER
        // For example: List<Order> findBySource(OrderSource source);
        // And then call it with OrderSource.USER
        // For now, let's assume CombinedOrderBookService will be adapted or this method will be refined.
        // A simple way for now, though less efficient if many non-user orders exist (which they don't in DB yet):
        return orderRepository.findAll().stream()
            .filter(order -> order.getSource() == OrderSource.USER)
            .collect(java.util.stream.Collectors.toList());
        // A better way would be: return orderRepository.findBySource(OrderSource.USER);
        // Add `List<Order> findBySource(OrderSource source);` to OrderRepository if this is preferred.
    }
}
