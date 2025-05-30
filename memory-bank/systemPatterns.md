# System Patterns: Web-Based Market Exchange

## 1. Overall Architecture

The system follows a client-server architecture:
-   **Frontend:** Angular single-page application (SPA) running in the user's browser.
-   **Backend:** Java Spring Boot application serving REST APIs and WebSocket connections.

```mermaid
graph TD
    UserBrowser[User Browser] --> AngularFE[Angular Frontend]
    
    subgraph BackendServer [Backend Server (Java Spring)]
        RESTController[REST API Controllers]
        WebSocketHandler[WebSocket Handler]
        BusinessServices[Business Logic Services]
        DataRepositories[Data Repositories]
        ExternalIntegrations[External API Integrations]
    end

    AngularFE -- HTTPS (REST for User Actions) --> RESTController
    AngularFE -- WSS (Live Data) --> WebSocketHandler
    
    RESTController --> BusinessServices
    WebSocketHandler -- Consumes & Broadcasts --> BusinessServices
    BusinessServices --> DataRepositories
    BusinessServices --> ExternalIntegrations

    ExternalIntegrations -- WebSocket/REST --> ExternalExchange[External Exchange (Binance)]
    DataRepositories -- Manages --> InMemoryDB[In-Memory Database (User Orders)]
```

## 2. Key Technical Decisions & Patterns

### Backend (Java Spring):
-   **Spring Boot:** For rapid application development and convention-over-configuration.
-   **Maven:** For dependency management and build automation.
-   **REST APIs (Spring Web MVC):** For synchronous user-initiated actions like placing orders, canceling orders, and fetching user-specific data.
    -   Pattern: Standard Controller-Service-Repository.
-   **WebSockets (Spring WebSocket):** For real-time, bidirectional communication of the combined order book data to the frontend.
    -   Backend pushes updates to connected clients.
-   **In-Memory Data Storage:** User orders are stored in-memory using concurrent-safe Java collections (`ConcurrentHashMap`, `NavigableMap` within `CopyOnWriteArrayList` for price levels) for simplicity and performance, suitable for a non-persistent simulation.
    -   `InMemoryUserOrderRepository` encapsulates this logic.
-   **External API Integration (Binance):**
    -   A dedicated service (`BinanceDataService`) connects to Binance's public WebSocket API (e.g., `btcusdt@depth5@100ms`) to receive live market depth updates.
    -   Uses `org.java-websocket` client library.
-   **Order Book Aggregation:**
    -   `CombinedOrderBookService` is responsible for fetching user orders and Binance data, merging them, and limiting to the top 5 bids/asks.
    -   This service is triggered by updates from `BinanceDataService` or changes in user orders via `UserOrderService`.
-   **Authentication (Spring Security):**
    -   Initial setup uses HTTP Basic Authentication with in-memory user details.
    -   Designed to be extensible for JWT or other schemes later.
-   **Dependency Injection:** Spring's DI is used throughout for managing bean lifecycles and dependencies.
    -   Setter injection and `@Lazy` are used to manage circular dependencies between services where necessary (e.g., `UserOrderService` and `CombinedOrderBookService`).
-   **JSON Messaging:** Jackson library (default with Spring Boot) for serializing/deserializing JSON payloads for REST and WebSockets.

### Frontend (Angular):
-   **Angular CLI:** For project generation, building, and development server.
-   **Component-Based Architecture:** UI is built using Angular components.
    -   `LoginComponent`, `OrderBookComponent`, `OrderEntryComponent`, `MyOrdersComponent`.
-   **Services:** Business logic and data fetching are encapsulated in services.
    -   `AuthService`: Manages authentication state, login/logout. Stores auth indication in `localStorage`.
    -   `OrderService`: Interacts with backend REST APIs for user order management.
    -   `WebSocketService`: Manages WebSocket connection to the backend for live order book data using `rxjs/webSocket`.
-   **Routing (Angular Router):** Manages navigation between different views/components.
-   **Reactive Programming (RxJS & Signals):**
    -   RxJS `Observable` for handling asynchronous operations like HTTP requests and WebSocket messages.
    -   Angular Signals (`signal`) for managing and reacting to state changes within components (e.g., `isLoading`, `errorMessage`, order book data).
-   **Zoneless Application (Developer Preview):** Chosen during project setup, aims for better performance by reducing reliance on Zone.js for change detection.
-   **SCSS:** For styling.
-   **HttpClientModule:** For making HTTP requests to the backend.
-   **FormsModule:** For handling forms (e.g., login, order entry).

## 3. Communication Flow for Live Order Book

1.  `BinanceDataService` (Backend) receives a depth update from Binance WebSocket.
2.  It updates its local cache of Binance's order book.
3.  It triggers a callback in `CombinedOrderBookService` (Backend).
4.  `CombinedOrderBookService` fetches current user orders from `InMemoryUserOrderRepository` and the updated Binance data from `BinanceDataService`.
5.  It aggregates these into a `CombinedOrderBook` object (top 5 bids/asks).
6.  `CombinedOrderBookService` calls `OrderBookWebSocketHandler.broadcastOrderBookUpdate()` (Backend).
7.  `OrderBookWebSocketHandler` serializes the `CombinedOrderBook` to JSON and sends it to all connected Angular clients via the backend's WebSocket.
8.  `WebSocketService` (Frontend) receives the JSON message, deserializes it.
9.  It pushes the message to its `messages$` observable/subject.
10. `OrderBookComponent` (Frontend), subscribed to `messages$`, receives the update.
11. `OrderBookComponent` updates its signals (`bids`, `asks`, `currentTradingPair`, `lastUpdated`), causing the UI to re-render reactively.

## 4. Error Handling

-   **Backend:** Standard Spring exception handling for REST APIs (e.g., `ResponseEntity` with error codes). WebSocket errors logged.
-   **Frontend:** Services use RxJS `catchError` for API/WebSocket errors. Components use signals (`errorMessage`) to display feedback to the user.

## 5. Future Considerations / Potential Patterns
-   **Authentication:** Transition from Basic Auth to JWT for better security and statelessness.
    -   Implement an HTTP interceptor in Angular to attach JWTs to requests.
-   **State Management (Frontend):** For more complex state, consider NgRx or other dedicated state management libraries if signals and services become insufficient.
-   **Database (Backend):** Replace in-memory storage with a persistent database (e.g., PostgreSQL, H2 for development) if data persistence is required.
-   **Scalability:** Consider message queues (e.g., RabbitMQ, Kafka) for decoupling services if the system grows.
-   **Testing:** Implement comprehensive unit, integration, and end-to-end tests.
