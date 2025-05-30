# Progress: Web-Based Market Exchange

## Current Date: 2025-05-30

## 1. What Works (Successfully Implemented & Tested)

### Backend:
-   **Core Application:** Spring Boot application starts successfully.
-   **Dependencies:** Maven dependencies resolve, and the project compiles (Java 1.8 target).
-   **Circular Dependencies:** Resolved between services.
-   **Data Models:** All core data models (`Order`, `User`, etc.) are defined.
-   **In-Memory Repository:** `InMemoryUserOrderRepository` for user orders is functional.
-   **Basic Security:** HTTP Basic Authentication with in-memory users (`user1`/`pass1`, `user2`/`pass2`) is active.
-   **REST API (User Orders):**
    -   `POST /api/orders` (create order): Tested successfully with `curl`. Authenticated users can create orders.
    -   `DELETE /api/orders/{orderId}` (cancel order): Skeleton implemented.
    -   `GET /api/orders/my-orders` (get user's orders): Skeleton implemented.
-   **Binance Integration:**
    -   `BinanceDataService` connects to Binance's public WebSocket stream (`wss://stream.binance.com:9443/ws/btcusdt@depth5@100ms`) upon application startup.
    -   Parses incoming depth messages (top 5 levels).
-   **Order Book Aggregation & Broadcast:**
    -   `CombinedOrderBookService` aggregates user and Binance data (logic in place).
    -   `OrderBookWebSocketHandler` is set up to broadcast these combined updates.
    -   Updates are triggered by `BinanceDataService` and `UserOrderService` (when orders are created/cancelled).

### Frontend:
-   **Project Setup:** Angular (zoneless) project `frontend-angular` created.
-   **Build Environment:** Node.js (v20.19.2) and Angular CLI are installed and functional.
-   **Dependencies:** `npm install` completed successfully.
-   **Development Server:** Angular dev server (`npm start`) runs and serves the application.
-   **Core Services (Skeletons):**
    -   `AuthService`: Basic login/logout logic, stores auth indication in `localStorage`. Login flow tested via browser.
    -   `WebSocketService`: Implemented with `rxjs/webSocket`, includes connection logic, message deserialization, and reconnection attempts. Logging enhanced.
    -   `OrderService`: Methods for REST API interaction (create, cancel, get my orders) are defined.
-   **Basic Components (Skeletons):**
    -   `LoginComponent`: HTML form and TS logic for login.
    -   `OrderBookComponent`: TS logic to connect to WebSocket and handle messages; basic HTML template to display data.
    -   `OrderEntryComponent`: HTML form and TS logic for submitting orders.
    -   `MyOrdersComponent`: TS logic to fetch user orders and cancel them; HTML template to display.
-   **Routing:** Basic routes for components are configured in `app.routes.ts`.
-   **App Structure:** Main `app.html` has basic layout with `router-outlet` and navigation links. `app.ts` imports `RouterLink`.
-   **HttpClient:** `provideHttpClient(withInterceptorsFromDi())` is configured.

## 2. What's Left to Build / Refine (Key Areas)

### Backend:
-   **Trading Pair Management:** Currently hardcoded to "BTCUSDT". Needs to be configurable/dynamic.
-   **Robustness:** More comprehensive error handling, input validation.
-   **Security:**
    -   Consider CSRF protection if form-based login or session cookies are used more extensively (currently disabled, typical for API-first/JWT).
    -   Full implementation of JWT if moving away from Basic Auth.
-   **Testing:** Unit and integration tests.

### Frontend:
-   **WebSocket Data Display:**
    -   **Crucial:** Debug why `OrderBookComponent` was stuck on "Loading..." during browser testing. Verify WebSocket messages are received, parsed, and update component signals correctly. Check browser console for errors.
-   **Authentication Flow & Security:**
    -   Implement `AuthGuard` to protect routes.
    -   Improve `AuthService` and `OrderService` for robust auth header management (currently simplified for Basic Auth).
    -   Implement conditional display of Login/Logout links in `app.html`.
    -   Securely manage tokens/credentials if not relying solely on browser Basic Auth caching.
-   **Component Functionality:**
    -   Complete implementation details for `OrderEntryComponent` (e.g., form validation, user feedback).
    -   Complete implementation details for `MyOrdersComponent` (e.g., display formatting, cancel confirmation).
    -   Ensure all components correctly interact with their respective services.
-   **Styling & UI/UX:**
    -   Apply SCSS styling for a "modern look and feel" and responsiveness across all components.
    -   Improve user feedback mechanisms (e.g., loading spinners, toast notifications for success/error).
-   **Error Handling:** Implement user-friendly display of errors from API calls or WebSocket issues.
-   **State Management:** Evaluate if current signal-based state is sufficient or if a more robust solution (e.g., NgRx) is needed as complexity grows.
-   **Testing:** Unit tests for services and components.

## 3. Current Overall Status

-   **Foundation Laid:** The basic architectural skeletons for both frontend and backend are established.
-   **Backend Core Functional:** Backend can run, connect to Binance, process internal orders, and is ready to serve data.
-   **Frontend Structure Ready:** Frontend has basic components, services, and routing, ready for detailed implementation and debugging of data flow.
-   **Key Challenge:** Ensuring seamless WebSocket communication and data display from backend to frontend is the immediate next hurdle.

## 4. Known Issues / Blockers

-   **Frontend WebSocket Display:** `OrderBookComponent` was not displaying live data in the last browser test (stuck on "Loading..."). This needs to be the top priority for debugging.
-   **Frontend Auth Header Management:** The way `OrderService` gets auth headers is a placeholder and needs a proper solution aligned with `AuthService`.
-   **Node.js Version Warning:** While Angular CLI installed, `npm` warned about Node.js v18.19.1 not being officially supported by the latest CLI (which prefers v20.11+). This was resolved by upgrading Node.js to v20.19.2 using nvm.

## 5. Evolution of Project Decisions

-   **Java Version:** Changed from 11 to 1.8 in `pom.xml` to resolve initial backend compilation issues.
-   **Circular Dependencies (Backend):** Addressed using `@Lazy` and setter injection.
-   **Node.js/npm Installation:** Encountered issues with `ng` and `npm` not being found. Installed `nodejs` and `npm` via `apt`, then installed Angular CLI.
-   **Node.js Version for Angular:** Updated Node.js from v18.x to v20.x using `nvm` to meet Angular CLI requirements.
-   **Angular Component Naming:** Adjusted router imports to use class names like `Login` instead of `LoginComponent` as generated by default in the zoneless setup.
-   **Browser Testing Paused:** Paused direct browser interaction to focus on completing the Memory Bank setup as requested. Will resume for debugging WebSocket issues.
