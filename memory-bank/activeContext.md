# Active Context: Web-Based Market Exchange

## 1. Current Work Focus

The current focus has been on establishing the initial project skeleton for both the backend (Java Spring) and frontend (Angular) applications. This includes:
- Setting up project structures.
- Defining core data models.
- Implementing basic services for authentication, data fetching, and WebSocket communication.
- Creating placeholder components and basic routing for the frontend.
- Ensuring the backend application can compile, run, and connect to the external Binance WebSocket.
- Ensuring the frontend application can compile and run.
- Initializing the Memory Bank files.

## 2. Recent Key Changes & Milestones

- **Backend:**
    - Successfully created and ran the Spring Boot application.
    - Resolved Java version compatibility issues in `pom.xml`.
    - Resolved circular dependencies between services using `@Lazy` and setter injection.
    - `BinanceDataService` successfully connects to Binance's WebSocket stream.
    - Basic REST API for order creation (`/api/orders`) tested successfully with `curl`.
    - In-memory user storage and WebSocket handler for broadcasting order book data are in place.
- **Frontend:**
    - Installed Node.js (v20.19.2) and npm (v10.8.2) using `nvm`.
    - Installed Angular CLI globally.
    - Generated a new Angular (zoneless) project: `frontend-angular`.
    - Installed npm dependencies for the Angular project.
    - Created initial services: `AuthService`, `WebSocketService`, `OrderService`.
    - Created basic components: `LoginComponent`, `OrderBookComponent`, `OrderEntryComponent`, `MyOrdersComponent`.
    - Configured basic routing in `app.routes.ts`.
    - Updated `app.config.ts` to provide `HttpClient`.
    - Updated main `app.html` and `app.ts` for basic layout and navigation.
    - Implemented initial logic in `LoginComponent` and `OrderBookComponent`.
    - Angular dev server (`npm start`) is running.
- **Memory Bank:**
    - `projectbrief.md` created.
    - `productContext.md` created.
    - `systemPatterns.md` created.
    - `techContext.md` created.
    - This `activeContext.md` file is being created.

## 3. Next Steps (Immediate)

1.  **Create `progress.md`:** The final core Memory Bank file.
2.  **Frontend - Refine WebSocket & Order Book Display:**
    -   Thoroughly test and debug the WebSocket connection from Angular to the Spring backend.
    -   Ensure `OrderBookComponent` correctly receives and displays live data.
    -   Address why the "Loading order book..." message was persisting in the browser test. This likely involves checking browser console logs for WebSocket errors or data format mismatches.
3.  **Frontend - Authentication Flow:**
    -   Implement an `AuthGuard` to protect routes.
    -   Refine `AuthService` and `OrderService` to handle Basic Auth headers more robustly (currently a placeholder/simplification). Consider how credentials/tokens are managed after login for subsequent API calls.
    -   Update navigation in `app.html` to conditionally show Login/Logout links.
4.  **Frontend - Component Implementation:**
    -   Flesh out `OrderEntryComponent` and `MyOrdersComponent` functionality.
    -   Add basic styling to all components.

## 4. Active Decisions & Considerations

-   **Authentication Scheme:** Currently using HTTP Basic Auth. This is simple for initial setup but has limitations. A transition to JWTs is a future consideration noted in `systemPatterns.md`. The current frontend auth header handling in `OrderService` is a known simplification that needs addressing.
-   **Zoneless Angular:** This is a developer preview feature. While it offers potential performance benefits, we need to be mindful of any specific patterns or limitations it imposes.
-   **Error Handling:** Basic logging and error signals are in place. More user-friendly error displays and comprehensive backend/frontend logging strategies will be needed.
-   **Styling:** No specific UI framework (like Angular Material or Bootstrap) has been decided upon yet. Initial styling will be custom SCSS.
-   **Trading Pair:** Currently defaulting to "BTCUSDT". Functionality for selecting other pairs is a future enhancement.

## 5. Important Patterns & Preferences Emerging

-   **Angular Signals:** Used for reactive state management within components.
-   **RxJS for Async:** Used for HTTP calls and WebSocket message streams.
-   **Service-Oriented Architecture (Frontend & Backend):** Logic is encapsulated in services.
-   **REST for Commands, WebSockets for Events:** User actions (create order) use REST; continuous data (order book) uses WebSockets.
-   **Incremental Development:** Building and testing features step-by-step.
