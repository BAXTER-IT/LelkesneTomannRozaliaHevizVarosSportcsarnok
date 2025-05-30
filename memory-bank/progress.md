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
-   **Authentication & Routing:**
    -   `AuthService` (`services/auth.ts`): Login logic functional. Logout logic now includes navigation to `/login`. `isAuthenticated` and `currentUser` signals provide reactive auth state.
    -   `AuthGuard` (`guards/auth.guard.ts`): Created and functional, protects routes based on `AuthService.isAuthenticated()`.
    -   `app.routes.ts`: Routes restructured for independent login page (`/login`) and authenticated application area (`/app/...`). Root and wildcard routes correctly redirect.
    -   `LoginComponent` (`components/login/login.ts`): Navigates to `/app/order-book` on successful login.
    -   `app.html` & `app.ts`: Main layout now conditionally renders header/footer and navigation links based on authentication state, providing a clean login page.
-   **UI/UX & Styling:**
    -   `styles.scss`: Global styles established, including CSS reset, default typography, CSS variables for theming, and base styles for common elements. SASS compilation error with `darken()` function resolved.
    -   `LoginComponent` (`components/login/login.scss`, `components/login/login.html`): Styled for a full-page, modern, and centered appearance. Includes loading indicator and error message styling.
-   **Core Services (Skeletons/Partially Implemented):**
    -   `WebSocketService`: Implemented with `rxjs/webSocket`, includes connection logic, message deserialization, and reconnection attempts. Logging enhanced. (Needs testing for data display).
    -   `OrderService`: Methods for REST API interaction (create, cancel, get my orders) are defined. (Auth header handling needs refinement).
-   **Basic Components (Skeletons/Partially Implemented):**
    -   `OrderBookComponent`: TS logic to connect to WebSocket and handle messages; basic HTML template to display data. (Needs data display debugging).
    -   `OrderEntryComponent`: HTML form and TS logic for submitting orders.
    -   `MyOrdersComponent`: TS logic to fetch user orders and cancel them; HTML template to display.
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
