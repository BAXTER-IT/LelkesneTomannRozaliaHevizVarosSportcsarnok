# Active Context: Web-Based Market Exchange

## 1. Current Work Focus

The primary focus is on establishing a complete and robust authentication flow, including backend support and frontend integration, and ensuring subsequent authenticated actions (like fetching user-specific data) can be performed.

## 2. Recent Key Changes & Milestones

- **Backend - Authentication:**
    -   Created `UserLoginResponseDTO.java` (`model/dto/`) to define the response structure for successful login.
    -   Implemented `AuthController.java` (`controller/`) with a new `POST /api/auth/login` endpoint.
        -   This endpoint uses Spring Security's existing HTTP Basic Authentication mechanism.
        -   On successful authentication, it returns the `UserLoginResponseDTO` containing the username.
    -   Updated `SecurityConfig.java` to include CORS configuration.
    -   Resolved backend port conflict (port 8080 was in use), backend now successfully starts on port 8080. `application.properties` confirmed/reverted to use port 8080.
- **Frontend - Authentication, CORS Proxy & Data Fetching:**
    -   Created `proxy.conf.json` to proxy API (`/api`) and WebSocket (`/ws`) requests from `http://localhost:4200` to `http://localhost:8080`. This was implemented to resolve persistent CORS/404 errors.
    -   Updated `angular.json` to use `proxy.conf.json` for the development server.
    -   Updated `package.json`'s `start` script to `ng serve --proxy-config proxy.conf.json` to ensure the proxy is always used during development.
    -   Updated `AuthService.ts`, `OrderService.ts`, and `WebSocketService.ts` to use relative base URLs (e.g., `/api/auth/login`, `/ws/market`) to work with the proxy.
    -   Defined `UserLoginResponseDTO` interface in `AuthService.ts`.
    -   Updated `AuthService.login()`:
        -   Now makes a `POST` request to the (proxied) `/api/auth/login` backend endpoint.
        -   Includes the Basic Authentication header.
        -   On successful response, stores user details in `localStorage` and updates `isAuthenticated` and `currentUser` signals.
        -   Returns an `Observable<UserLoginResponseDTO | null>`.
    -   Updated `LoginComponent` (`components/login/login.ts`):
        -   Injects `OrderService`.
        -   Subscribes to the updated `authService.login()`.
        -   On successful login (receiving `UserLoginResponseDTO`):
            -   Navigates to `/app/order-book`.
            -   Calls `orderService.getMyOrders()` to fetch the user's orders.
- **Previously Completed (Frontend - Authentication & Routing):**
    -   `AuthGuard` (`guards/auth.guard.ts`) created to protect routes.
    -   `app.routes.ts` restructured for `/login` and authenticated `/app` routes.
    -   `AuthService` logout navigates to `/login`.
    -   `app.html` and `app.ts` conditionally render UI based on authentication.
- **Previously Completed (Frontend - UI/UX & Styling):**
    -   Global styles in `styles.scss` and `LoginComponent` styling.
    -   SASS compilation error resolved.
- **Previously Completed (Backend Setup):**
    -   Spring Boot application now successfully runs on port 8080, dependencies resolved, circular dependencies handled.
    -   `BinanceDataService` connects.
    -   Basic order creation API and WebSocket handler in place.
- **Memory Bank:**
    -   All core files are actively being updated to reflect progress.

## 3. Next Steps (Immediate)

1.  **Frontend - Continue UI/UX Enhancements:**
    -   Style the main application shell (`app.html`, `app.scss`) for authenticated users.
    -   Gradually apply modern styling and responsiveness to `OrderBookComponent`, `OrderEntryComponent`, and `MyOrdersComponent`.
    -   Improve user feedback mechanisms (e.g., loading spinners, toast notifications) across the application.
2.  **Frontend - Refine WebSocket & Order Book Display:**
    -   Thoroughly test and debug the WebSocket connection from Angular to the Spring backend.
    -   Ensure `OrderBookComponent` correctly receives and displays live data.
    -   Address any persisting "Loading order book..." messages by checking browser console logs for WebSocket errors or data format mismatches.
3.  **Frontend - Component Functionality:**
    -   Flesh out `OrderEntryComponent` and `MyOrdersComponent` functionality (form validation, interaction with services).
4.  **Memory Bank:** Ensure `progress.md` is updated to reflect the latest changes.

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
