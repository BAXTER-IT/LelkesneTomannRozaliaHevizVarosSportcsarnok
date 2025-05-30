package com.exchange.marketexchange.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class BinanceDepthStreamPayload {

    @JsonProperty("lastUpdateId")
    private long lastUpdateId;

    @JsonProperty("bids")
    private List<List<String>> bids; // List of [price, quantity]

    @JsonProperty("asks")
    private List<List<String>> asks; // List of [price, quantity]

    // Helper methods to convert to our OrderBookEntry model could be added here
    // or in the service that uses this DTO.
}
