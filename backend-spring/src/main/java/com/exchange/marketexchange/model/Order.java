package com.exchange.marketexchange.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.Column;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID; // For generating orderId if not set

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_order") // Renamed to avoid SQL keyword conflict and specify it's user's order
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Database primary key

    @Column(unique = true, nullable = false)
    private String orderId; // Business key (UUID as String)

    @ManyToOne // Many orders can belong to one user
    @JoinColumn(name = "app_user_id", nullable = false) // Foreign key column in user_order table
    private User user; // Changed from String userId to User user

    @Enumerated(EnumType.STRING)
    private OrderType type;

    private BigDecimal price;
    private BigDecimal quantity;
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING) // Assuming OrderSource is an enum
    private OrderSource source; // To distinguish between USER and BINANCE orders

    private String tradingPair; // e.g., "BTCUSDT"

    // Custom constructor or a method to initialize orderId if needed,
    // especially if we want to ensure it's set before persisting if not already.
    // For example, a pre-persist method or ensuring it's set in the service layer.
    // Lombok's @NoArgsConstructor and @AllArgsConstructor are present.
    // We might need a constructor that sets the user and other fields,
    // and perhaps auto-generates orderId if it's not provided.

    public Order(User user, OrderType type, BigDecimal price, BigDecimal quantity, String tradingPair, OrderSource source) {
        this.orderId = UUID.randomUUID().toString(); // Ensure orderId is always generated for new user orders
        this.user = user;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();
        this.tradingPair = tradingPair;
        this.source = source; // Should be OrderSource.USER for orders created by our users
    }
}
