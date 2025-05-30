package com.exchange.marketexchange.repository;

import com.exchange.marketexchange.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> { // ID type is Long (for the auto-generated id)
    List<Order> findByUser_UsernameOrderByTimestampDesc(String username);
    Optional<Order> findByOrderIdAndUser_Username(String orderId, String username);
}
