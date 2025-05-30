package com.exchange.marketexchange.repository;

import com.exchange.marketexchange.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> { // ID type is String (for userId UUID)
    Optional<User> findByUsername(String username);
}
