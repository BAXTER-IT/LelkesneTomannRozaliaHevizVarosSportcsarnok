package com.exchange.marketexchange.controller;

import com.exchange.marketexchange.model.Order;
import com.exchange.marketexchange.service.UserOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final UserOrderService userOrderService;

    @Autowired
    public OrderController(UserOrderService userOrderService) {
        this.userOrderService = userOrderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order orderRequest, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Order createdOrder = userOrderService.createOrder(orderRequest, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // Or return error message
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean cancelled = userOrderService.cancelOrder(orderId, userDetails.getUsername());
        if (cancelled) {
            return ResponseEntity.ok().build();
        } else {
            // Could be not found, or not authorized, or not a user order
            // For simplicity, returning 404, but could be more specific
            return ResponseEntity.notFound().build(); 
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Order> myOrders = userOrderService.getOrdersByUsername(userDetails.getUsername()); // Updated method name
        return ResponseEntity.ok(myOrders);
    }

    // Optional: Endpoint to get a specific order by ID (if needed by client)
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable String orderId, @AuthenticationPrincipal UserDetails userDetails) {
         if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Fetch order by its business orderId and ensure it belongs to the authenticated user
        return userOrderService.getOrderByOrderIdAndUsername(orderId, userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
