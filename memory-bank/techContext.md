# Tech Context: Web-Based Market Exchange

## 1. Technologies Used

### Backend:
-   **Language:** Java (Version 1.8 as per `pom.xml`)
-   **Framework:** Spring Boot (Version 2.7.18)
    -   Spring Web MVC (for REST APIs)
    -   Spring WebSocket
    -   Spring Security (for authentication)
-   **Build Tool:** Apache Maven
-   **WebSocket Client Library (for Binance):** `org.java-websocket/Java-WebSocket`
-   **JSON Processing:** Jackson (comes with Spring Boot)
-   **Database:** H2 (file-based persistence)
-   **Data Access:** Spring Data JPA

### Frontend:
-   **Framework:** Angular (Version corresponding to global CLI, likely latest stable e.g., v17/v18, but project generated with Node 20 compatibility)
    -   Angular CLI
    -   Angular Router
    -   Angular HttpClientModule
    -   Angular FormsModule
    -   Angular Signals
    -   Zoneless Application (Developer Preview)
-   **Language:** TypeScript
-   **Styling:** SCSS
-   **Package Manager:** npm (Node Package Manager, version v10.8.2 via nvm)
-   **WebSocket Client Library:** RxJS `webSocket`

### Development Environment & Tools:
-   **Node.js Version Manager:** `nvm`
-   **Node.js:** Version 20.19.2 (managed by nvm)
-   **Operating System (Development):** Linux (as per environment details)
-   **IDE/Editor:** VS Code (implied by user interaction)
-   **Version Control:** Git (implied, though not explicitly used in interactions yet)

## 2. Development Setup

-   **Backend:**
    -   Requires JDK 1.8 or newer.
    -   Requires Apache Maven.
    -   Run with `cd backend-spring && mvn spring-boot:run`.
    -   Serves on `http://localhost:8080`.
-   **Frontend:**
    -   Requires Node.js (v20.11+ recommended by Angular CLI, currently using v20.19.2 via nvm) and npm.
    -   Requires Angular CLI installed globally (`sudo npm install -g @angular/cli`).
    -   Run with `cd frontend-angular && npm start`.
    -   Serves on `http://localhost:4200` (or an alternative port if 4200 is busy, e.g., 42365).
-   **External Dependencies:**
    -   Internet connection required for Binance WebSocket stream.

## 3. Technical Constraints & Considerations

-   **H2 Database Persistence:** User order data is now persisted to an H2 file (`./data/marketexchange_db`).
-   **Single Trading Pair Focus (Initially):** The Binance integration and frontend display are initially focused on a single, likely hardcoded or default trading pair (e.g., "BTCUSDT").
-   **Basic Authentication:** Current security uses HTTP Basic Auth. This has limitations (e.g., sending credentials with each request if not handled by browser session, harder to securely "logout" without closing browser). Planned for future JWT enhancement.
-   **Error Handling:** Basic error handling is in place; more sophisticated user-facing error messages and logging can be added.
-   **Responsiveness & Styling:** Frontend components are structurally created but require SCSS styling for a "modern look and feel" and responsiveness.
-   **Zoneless Angular:** Being a developer preview feature, it might have some edge cases or require specific patterns compared to Zone.js-based Angular apps.
-   **Node.js Version for Angular CLI:** The globally installed Angular CLI (latest) prefers newer Node.js versions than what was initially available via `apt`. `nvm` was used to manage this.

## 4. Key Dependencies (Summary from Build Files)

### Backend (`pom.xml`):
-   `spring-boot-starter-web`
-   `spring-boot-starter-websocket`
-   `spring-boot-starter-security`
-   `spring-boot-starter-data-jpa` (New)
-   `com.h2database:h2` (New)
-   `org.projectlombok:lombok` (optional)
-   `org.java-websocket:Java-WebSocket` (for Binance client)
-   `io.jsonwebtoken:jjwt-*` (for potential JWT use later)
-   Spring Boot Test starters.

### Frontend (`package.json` - standard Angular +):
-   `@angular/animations`
-   `@angular/common`
-   `@angular/compiler`
-   `@angular/core`
-   `@angular/forms`
-   `@angular/platform-browser`
-   `@angular/router`
-   `rxjs`
-   `tslib`
-   (Dev Dependencies: `@angular-devkit/build-angular`, `@angular/cli`, `@angular/compiler-cli`, `typescript`)

This document should be updated as new technologies are introduced or existing ones are configured differently.
