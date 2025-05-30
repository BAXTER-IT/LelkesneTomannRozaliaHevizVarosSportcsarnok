# Project Brief: Web-Based Market Exchange

## 1. Project Overview

The goal is to create a web-based market exchange platform with an integrated order book. This system will allow users to place their own orders and view a combined list of these orders alongside data fetched from an external cryptocurrency exchange.

## 2. Core Requirements

### General:
- **Combined Order Book:** Display user's orders and orders from an external exchange in a unified view.
- **Multiple Users:** The system must support multiple concurrent users.
- **Error Handling:** Robust error handling is required on both the frontend and backend.
- **External Exchange Integration:** Query crypto order data from an exchange that does not require authentication. Research is needed to identify suitable exchanges (e.g., Binance, Bitfinex, Huobi) and their public API capabilities for order book data.

### Frontend:
- **Technology:** Angular.
- **Responsiveness:** Modern, responsive user interface.
- **Live Data:** Automatic updates using WebSocket for live data display.
- **Authentication:** Users must authenticate to view prices and interact with the order book.
- **Backend Communication:** Connection to the backend via WebSocket.

### Backend:
- **Technology:** Java Spring application.
- **Build Tool:** Maven (pom.xml to be created).
- **Database:** In-memory database.
- **API Endpoint (`/depth`):**
    - Lists combined orders (user orders + external exchange orders).
    - Filters orders to show only the top 5 bids and asks.
- **Frontend Communication:** Communication via WebSocket, sending JSON messages.

## 3. Key Goals

- Develop a functional frontend for users to interact with the order book.
- Implement a robust backend to manage orders, user data, and external exchange integration.
- Ensure real-time data synchronization between frontend and backend.
- Provide a secure authentication mechanism.
- Successfully integrate with a public crypto exchange API for order book data.
