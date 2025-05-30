package com.exchange.marketexchange.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
// import javax.persistence.OneToMany; // Optional: if we add a list of orders
// import javax.persistence.CascadeType;
// import javax.persistence.FetchType;
// import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_user") // "user" can be a reserved keyword in SQL
public class User {

    @Id
    @Column(name = "id") // Explicitly naming the column for clarity
    private String userId; // This will be our primary key (UUID as String)

    @Column(unique = true, nullable = false)
    private String username;

    // Password hash is not directly stored if using Spring Security's InMemoryUserDetailsManager for auth
    // But if we were to manage users fully in DB, this would be relevant.
    // For now, we'll keep it as it might be used if we switch auth later.
    private String passwordHash; 

    // Optional: If we want a direct relationship from User to their Orders
    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<Order> orders;

    // We can add roles or other user-specific details later if needed
}
