# Active Context: Web-Based Market Exchange

## 1. Current Work Focus

The backend has been updated to use H2 for data persistence, replacing the in-memory solution. The immediate focus is to test this persistence and then shift to frontend tasks, particularly WebSocket integration and component functionality.

## 2. Recent Key Changes & Milestones

- **Backend - H2 Database Persistence for Orders:**
    -   **Dependencies:** Added `spring-boot-starter-data-jpa` and `com.h2database:h2` to `pom.xml`.
    -   **Configuration:** Updated `application.properties` to configure H2 to save to a file (`./data/marketexchange_db`) and enabled the H2 console (`/h2-console`).
    -   **Entities:**
        -   `User.java`: Annotated as `@Entity`, `@Table(name = "app_user")`. `userId` (String UUID) is `@Id`.
        -   `Order.java`: Annotated as `@Entity`, `@Table(name = "user_order")`. Added `Long id` as `@Id @GeneratedValue`. `orderId` (String UUID) is a unique business key. Changed `userId` (String) to `User user` (`@ManyToOne` relationship). `OrderType` and `OrderSource` enums are stored as strings.
    -   **Repositories:**
        -   Created `UserRepository.java` (extends `JpaRepository<User, String>`) with `findByUsername`.
        -   Created `OrderRepository.java` (extends `JpaRepository<Order, Long>`) with `findByUser_UsernameOrderByTimestampDesc` and `findByOrderIdAndUser_Username`.
        -   Deleted `InMemoryUserOrderRepository.java`.
    -   **Services:**
        -   `UserOrderService.java`:
            -   Injected `OrderRepository` and `UserRepository`.
            -   `createOrder`: Now finds/creates a `User` entity and associates it with the `Order`. Orders are saved via `orderRepository`.
            -   `cancelOrder`: Uses `orderRepository` to find and delete orders.
            -   `getOrdersByUsername`: (Renamed from `getOrdersByUserId`) Uses `orderRepository`.
            -   Added `getOrderByOrderIdAndUsername` for fetching a specific user order by its business ID.
            -   `getAllUserPersistedOrders`: Fetches all orders with `OrderSource.USER` for `CombinedOrderBookService`.
        -   `CombinedOrderBookService.java`:
            -   Constructor and fields updated to use `UserOrderService` instead of `InMemoryUserOrderRepository`.
            -   `getCombinedOrderBook`: Fetches user orders via `userOrderService.getAllUserPersistedOrders()`, then filters and processes them to build `userBidsMap` and `userAsksMap`.
    -   **Controller:**
        -   `OrderController.java`:
            -   `getMyOrders`: Updated to call `userOrderService.getOrdersByUsername()`.
            -   `getOrderById`: Updated to call `userOrderService.getOrderByOrderIdAndUsername()` for fetching a specific order by its business ID, scoped to the authenticated user.
- **Previously (Backend - Authentication):**
    -   Created `UserLoginResponseDTO.java`.
    -   Implemented `AuthController.java` with `POST /api/auth/login`.
    -   Updated `SecurityConfig.java` for CORS.
- **Previously (Frontend - Authentication, CORS Proxy & Data Fetching):**
    -   `proxy.conf.json` and related Angular configurations for backend communication.
    -   `AuthService.login()` updated for backend calls.
    -   `LoginComponent` calls `orderService.getMyOrders()` post-login.
- **Previously (Frontend - UI/UX & Styling):**
    -   Extensive styling updates to global styles, app shell, and components (`OrderBookComponent`, `OrderEntryComponent`, `MyOrdersComponent`, `LoginComponent`).
    -   Differentiation of user orders in the order book display.
- **Previously (Backend Setup):**
    -   Core Spring Boot application setup, Binance connection.
- **Memory Bank:**
    -   All core files are actively being updated to reflect progress.
- **Frontend - Order Book Page Enhancement:**
    -   **`OrderBookComponent` (`order-book.ts`, `order-book.html`, `order-book.scss`):**
        -   Added an "Add New Order" button.
        -   Implemented functionality to conditionally display the `OrderEntryComponent` alongside the order book when the button is clicked.
        -   Updated SCSS for the new button and layout, including responsive adjustments.
    -   Imported `OrderEntry` component into `OrderBookComponent`.
- **Git Configuration:**
    -   Created a root `.gitignore` file.
    -   Added `backend-spring/target/` to `.gitignore` to exclude Maven build artifacts.
- **Frontend - Order Entry Form Enhancement (Market/Limit Orders):**
    -   **`OrderEntryComponent` (`order-entry.ts`):**
        -   Added `orderTypeSelected` signal (default 'market').
        -   Added `@Input() bestBidPrice` and `@Input() bestAskPrice`.
        -   Implemented an `effect` to auto-set `orderRequest.price` for 'market' orders based on `orderRequest.type` (BUY/SELL) and `bestBidPrice`/`bestAskPrice`.
    -   **`OrderEntryComponent` (`order-entry.html`):**
        -   Added radio buttons for "Market" and "Limit" selection, bound to `orderTypeSelected`.
        -   Price input field is now `[disabled]` and `[readonly]` when `orderTypeSelected()` is 'market'.
    -   **`OrderBookComponent` (`order-book.ts`):**
        -   Added `computed` signals `bestBidPrice` and `bestAskPrice` derived from the live `bids` and `asks` signals.
    -   **`OrderBookComponent` (`order-book.html`):**
        -   The `<app-order-entry>` tag now passes `bestBidPrice()` and `bestAskPrice()` as inputs.
    -   **`OrderEntryComponent` (`order-entry.scss`):**
        -   Added styles for the radio button group to appear as toggle buttons.
        -   Enhanced styles for the disabled/readonly price input for better visual distinction.
        -   Corrected layout: "Order Type:" label is now above the "Market" and "Limit" buttons, which are themselves arranged horizontally.
    -   **Global Styles (`styles.scss`):**
        -   Defined `--button-text-color` as white and applied it to global button styles for better contrast on primary-colored buttons.
    -   **`OrderBookComponent` (`order-book.scss`):**
        -   Corrected the hover effect for the "Show/Hide Order Form" button to use SASS `darken` function, preventing it from disappearing.

## 3. Next Steps (Immediate)

1.  **Backend - Test H2 Persistence:**
    -   Run the backend (`mvn spring-boot:run`).
    -   Create some orders via API (e.g., using `curl` or by logging in through the frontend if it's functional enough).
    -   Stop and restart the backend.
    -   Verify that previously created orders are still present (e.g., by fetching them via API or checking the H2 console at `http://localhost:8080/h2-console`).
2.  **Frontend - Refine WebSocket & Order Book Display:**
    -   Thoroughly test and debug the WebSocket connection from Angular to the Spring backend.
    -   Ensure `OrderBookComponent` correctly receives and displays live data.
    -   Address any persisting "Loading order book..." messages by checking browser console logs for WebSocket errors or data format mismatches.
3.  **Frontend - Component Functionality:**
    -   Flesh out `OrderEntryComponent` and `MyOrdersComponent` functionality (form validation, interaction with services).
4.  **Frontend - UI/UX Enhancements (Further):**
    -   Improve user feedback mechanisms.
5.  **Memory Bank:** Ensure `progress.md` and other relevant files are fully updated post-H2 integration and testing.

## 4. Active Decisions & Considerations

-   **Authentication Scheme:** Still using HTTP Basic Auth. While functional for the current stage, the plan to transition to JWTs (noted in `systemPatterns.md`) remains for better security and session management. The `AuthService.getAuthHeaders()` method is still a placeholder and needs to be addressed when components require authenticated API calls beyond what the browser handles automatically with Basic Auth.
-   **Zoneless Angular:** Continues to be used. No specific issues encountered related to it in the recent changes.
-   **Error Handling:** Basic error display is in place for login. Needs to be expanded for other interactions (API calls, WebSocket issues).
-   **Styling Approach:** Custom SCSS with CSS variables is the current approach. No decision yet on incorporating a full UI framework (e.g., Angular Material, Bootstrap).
-   **Trading Pair:** Remains defaulted to "BTCUSDT".

## 5. Important Patterns & Preferences Emerging

-   **Angular Signals:** Heavily used for reactive state management within components (`isAuthenticated`, `currentUser`, `isLoading`, `errorMessage`).
-   **Angular `inject()` function:** Preferred for dependency injection in services and guards.
-   **RxJS for Async:** Standard for HTTP calls (`AuthService.login`) and WebSocket interactions (in `WebSocketService`).
-   **Service-Oriented Architecture (Frontend & Backend):** Core logic remains encapsulated in services.
-   **Route Guards:** `AuthGuard` implemented for protecting authenticated routes.
-   **Conditional Rendering (`@if`):** Used in `app.html` for showing/hiding UI elements based on authentication state.
-   **CSS Custom Properties (Variables):** Used extensively in `styles.scss` for theming and maintainable styling.
-   **SASS Variables for Compile-Time Operations:** Used to resolve issues with SASS functions like `darken()` needing literal color values.
-   **Incremental Development & Refinement:** Tasks are broken down, implemented, and then refined based on feedback or issues (e.g., SASS error, extra login link).
