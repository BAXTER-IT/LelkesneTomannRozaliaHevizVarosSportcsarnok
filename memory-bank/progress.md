# Progress: Web-Based Market Exchange

## Current Date: 2025-05-30 (Updated)

## 1. What Works (Successfully Implemented & Tested)

### Backend:
-   **Core Application:** Spring Boot application now starts successfully on port 8080 (port conflict resolved).
-   **Dependencies:** Maven dependencies resolve, and the project compiles (Java 1.8 target).
-   **Circular Dependencies:** Resolved between services.
-   **Data Models:** All core data models (`Order`, `User`, etc.) are defined. `UserLoginResponseDTO` added.
-   **In-Memory Repository:** `InMemoryUserOrderRepository` for user orders is functional.
-   **Security & Authentication:**
    -   HTTP Basic Authentication with in-memory users (`user1`/`pass1`, `user2`/`pass2`) is active.
    -   **New:** `AuthController` with `POST /api/auth/login` endpoint implemented. Successfully returns `UserLoginResponseDTO` on valid Basic Auth.
    -   **Updated:** `SecurityConfig.java` includes CORS configuration. `application.properties` confirmed to use port 8080.
-   **REST API (User Orders):**
    -   `POST /api/orders` (create order): Tested successfully with `curl`. Authenticated users can create orders.
    -   `DELETE /api/orders/{orderId}` (cancel order): Skeleton implemented.
    -   `GET /api/orders/my-orders` (get user's orders): Skeleton implemented. This endpoint is now called by the frontend after successful login.
-   **Binance Integration:**
    -   `BinanceDataService` connects to Binance's public WebSocket stream.
    -   Parses incoming depth messages.
-   **Order Book Aggregation & Broadcast:**
    -   `CombinedOrderBookService` aggregates user and Binance data.
    -   `OrderBookWebSocketHandler` broadcasts combined updates.

### Frontend:
-   **Project Setup:** Angular (zoneless) project `frontend-angular` created.
-   **Build Environment:** Node.js (v20.19.2) and Angular CLI functional.
-   **Dependencies:** `npm install` completed.
-   **Development Server:** Angular dev server (`npm start`) runs.
    -   **New:** Configured with `proxy.conf.json` in `angular.json` to proxy `/api` and `/ws` requests to `http://localhost:8080`.
    -   **Updated:** `package.json`'s `start` script modified to `ng serve --proxy-config proxy.conf.json` to ensure proxy is always active for `npm start`.
-   **Authentication & Routing:**
    -   **Updated `AuthService` (`services/auth.ts`):**
        -   `UserLoginResponseDTO` interface defined.
        -   `login()` method now calls the (proxied) `POST /api/auth/login` backend endpoint.
        -   Base URL updated to be relative for proxy usage.
        -   Correctly handles success (updates `localStorage`, `isAuthenticated` & `currentUser` signals) and error (calls `logout`).
        -   Returns `Observable<UserLoginResponseDTO | null>`.
    -   `AuthGuard` (`guards/auth.guard.ts`): Protects routes.
    -   `app.routes.ts`: Routes for `/login` and `/app/...` structured.
    -   **Updated `LoginComponent` (`components/login/login.ts`):**
        -   Injects `OrderService`.
        -   Subscribes to the new `authService.login()` method.
        -   On successful login, navigates to `/app/order-book` and **now calls `orderService.getMyOrders()`**.
    -   `app.html` & `app.ts`: Conditional rendering based on auth state.
-   **UI/UX & Styling:**
    -   `styles.scss`: Global styles and theming variables.
    -   `LoginComponent`: Styled with loading/error states.
-   **Core Services:**
    -   **Updated `OrderService` (`services/order.ts`):** Base URL updated to be relative for proxy usage. `getMyOrders()` is now actively used post-login. (Auth header handling within `OrderService` itself still needs refinement).
    -   **Updated `WebSocketService` (`services/websocket.ts`):** WebSocket URL (`wsUrl`) updated to be relative to `window.location.host` to work with the proxy. (Needs full data display testing).
-   **Basic Components (Skeletons/Partially Implemented):**
    -   `OrderBookComponent`: (Needs data display debugging).
    -   `OrderEntryComponent`.
    -   `MyOrdersComponent`.
-   **HttpClient:** Configured.

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
    -   Improve `AuthService` and `OrderService` for robust auth header management for API calls (currently simplified for Basic Auth; `AuthService.getAuthHeaders()` is a placeholder).
    -   Securely manage tokens/credentials if/when transitioning from Basic Auth to JWTs.
-   **Component Functionality:**
    -   Complete implementation details for `OrderEntryComponent` (e.g., form validation, user feedback, interaction with `OrderService`).
    -   Complete implementation details for `MyOrdersComponent` (e.g., display formatting, cancel confirmation, interaction with `OrderService`).
    -   Ensure all components correctly interact with their respective services.
-   **Styling & UI/UX (Beyond Login Page):**
    -   Style the main application shell (`app.html`, `app.scss`) for authenticated users.
    -   Apply SCSS styling for a "modern look and feel" and responsiveness to `OrderBookComponent`, `OrderEntryComponent`, and `MyOrdersComponent`.
    -   Improve user feedback mechanisms (e.g., loading spinners, toast notifications for success/error) across the application.
-   **Error Handling:** Implement user-friendly display of errors from API calls or WebSocket issues in components other than Login.
-   **State Management:** Evaluate if current signal-based state is sufficient or if a more robust solution (e.g., NgRx) is needed as complexity grows.
-   **Testing:** Unit tests for services and components.

## 3. Current Overall Status

-   **Foundation Laid:** The basic architectural skeletons for both frontend and backend are established.
-   **Backend Core Functional:** Backend can run, connect to Binance, process internal orders, and is ready to serve data.
-   **Frontend Structure Ready:** Frontend has basic components, services, and routing, ready for detailed implementation and debugging of data flow.
-   **Key Challenge:** Ensuring seamless WebSocket communication and data display from backend to frontend is the immediate next hurdle.

## 4. Known Issues / Blockers

-   **Frontend WebSocket Display:** `OrderBookComponent` was not displaying live data in the last browser test (stuck on "Loading..."). This needs to be the top priority for debugging.
-   **Frontend Auth Header Management:** The `AuthService.getAuthHeaders()` method is a placeholder and needs a proper solution for services like `OrderService` to make authenticated API calls.
-   **CORS/404 Error on Login:** (Resolved) Initially a CORS error, then a 404. Resolved by:
    1.  Ensuring backend Spring Boot application runs successfully on port 8080 (resolved port conflict).
    2.  Implementing an Angular proxy (`proxy.conf.json`) to route API and WebSocket requests.
    3.  Updating frontend services to use relative URLs.
    4.  Updating `package.json` start script to ensure proxy is used.
    5.  (User needs to ensure Angular dev server is restarted after proxy config changes).
-   **Node.js Version Warning:** (Resolved) While Angular CLI installed, `npm` warned about Node.js v18.19.1 not being officially supported by the latest CLI (which prefers v20.11+). This was resolved by upgrading Node.js to v20.19.2 using nvm.

## 5. Evolution of Project Decisions

-   **Java Version:** Changed from 11 to 1.8 in `pom.xml` to resolve initial backend compilation issues.
-   **Circular Dependencies (Backend):** Addressed using `@Lazy` and setter injection.
-   **Node.js/npm Installation:** (Historical) Encountered issues with `ng` and `npm` not being found. Installed `nodejs` and `npm` via `apt`, then installed Angular CLI.
-   **Node.js Version for Angular:** (Historical) Updated Node.js from v18.x to v20.x using `nvm` to meet Angular CLI requirements.
-   **Angular Component Naming:** (Historical) Adjusted router imports to use class names like `Login` instead of `LoginComponent` as generated by default in the zoneless setup.
-   **Browser Testing Paused:** (Historical) Paused direct browser interaction to focus on completing the Memory Bank setup.
-   **SASS `darken()` with CSS Variables:** Resolved by introducing a SASS variable for compile-time processing.
-   **Login Page UI:** Refined to be a standalone view by conditionally rendering the main app header/footer.
-   **CORS Handling:** Shifted from backend-only CORS configuration to a frontend proxy (`proxy.conf.json`) for the Angular development server due to persistent preflight/404 issues.
-   **Backend Port:** Temporarily changed to 8081 due to port conflict, then reverted to 8080 after user indicated the port might be free.
