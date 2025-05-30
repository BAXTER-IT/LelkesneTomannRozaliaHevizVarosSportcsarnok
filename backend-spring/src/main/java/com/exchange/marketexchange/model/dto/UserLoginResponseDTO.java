package com.exchange.marketexchange.model.dto;

public class UserLoginResponseDTO {
    private String username;

    public UserLoginResponseDTO(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
