# Active Context: Web-Based Market Exchange

## 1. Current Work Focus

The primary focus has shifted to refining the frontend, specifically:
- Implementing a robust authentication flow with an independent login page.
- Improving the UI/UX, starting with the login page and global styling.
- Ensuring the application correctly handles authenticated and unauthenticated states.
- Addressing SASS compilation issues.

## 2. Recent Key Changes & Milestones

- **Frontend - Authentication & Routing:**
    -   Created `AuthGuard` (`guards/auth.guard.ts`) to protect routes requiring authentication.
    -   Restructured `app.routes.ts`:
        -   Established `/login` as the dedicated, unprotected login route.
        -   Created `/app` as the main authenticated route, with child routes for `order-book`, `my-orders`, and `trade`.
        -   Root (`/`) and wildcard (`**`) paths now redirect to `/app`, with `AuthGuard` handling redirection to `/login` if unauthenticated.
    -   `LoginComponent` (`components/login/login.ts`) now navigates to `/app/order-book` on successful login.
    -   `AuthService` (`services/auth.ts`) now injects `Router` and navigates to `/login` on logout.
    -   Updated `app.html` and `app.ts` to:
        -   Conditionally display main navigation links and Logout button only when authenticated.
        -   Conditionally render the main `<header>` and `<footer>` only when authenticated, providing a clean view for the `LoginComponent`.
- **Frontend - UI/UX & Styling:**
    -   Added global styles to `styles.scss`, including a basic reset, default typography, CSS variables for theming (colors, spacing, fonts), and base styles for common elements (links, buttons, inputs).
    -   Styled `LoginComponent` (`components/login/login.scss` and `components/login/login.html`):
        -   Made the login page full-height and centered the form.
        -   Applied modern styling to the form container, input groups, button, error messages, and added a loading indicator.
    -   Resolved SASS compilation error in `styles.scss` by using a SASS variable (`$primary-color-value`) for the `darken()` function, ensuring compile-time color processing.
- **Backend:**
    -   (No recent changes, focus has been on frontend)
    -   Successfully created and ran the Spring Boot application.
    -   Resolved Java version compatibility issues in `pom.xml`.
    -   Resolved circular dependencies between services using `@Lazy` and setter injection.
    -   `BinanceDataService` successfully connects to Binance's WebSocket stream.
    -   Basic REST API for order creation (`/api/orders`) tested successfully with `curl`.
    -   In-memory user storage and WebSocket handler for broadcasting order book data are in place.
- **Memory Bank:**
    -   All core files (`projectbrief.md`, `productContext.md`, `systemPatterns.md`, `techContext.md`, `progress.md`, `activeContext.md`) are now created and being updated.

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
