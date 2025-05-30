package com.exchange.marketexchange.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String userId;
    private String username;
    private String passwordHash;
    // We can add roles or other user-specific details later if needed
}
