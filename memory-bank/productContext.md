# Product Context: Web-Based Market Exchange

## 1. Purpose of the Project

The primary purpose of this project is to provide users with a platform to:
- Engage in a simulated market environment by placing their own buy and sell orders for cryptocurrencies.
- Gain real-time insights into market dynamics by viewing their orders alongside live order book data from a major external cryptocurrency exchange (Binance).
- Experience a simplified yet functional trading interface that updates dynamically.

This platform is envisioned as an educational tool, a sandbox for testing trading ideas, or for users who desire a consolidated view of their intended trades against a broader market backdrop without executing real trades on an external exchange.

## 2. Problems It Solves

- **Information Siloing:** Traders often monitor external exchange data while managing their own separate list of intended (but not yet placed) orders. This tool combines these views.
- **Lack of Safe Sandboxes:** New traders or those testing strategies may not want to risk real capital. This provides a simulated environment with real market data context.
- **Complexity of Full Exchange Interfaces:** Full exchange UIs can be overwhelming. This project aims for a simpler, more focused interface for order book interaction and personal order management.
- **Understanding Market Depth:** Visualizing personal order intentions alongside actual market depth can help users understand potential execution prices and market liquidity.

## 3. How It Should Work (User Perspective)

1.  **Authentication:** Users must register and log in to access the platform. Unauthenticated users should not see price data or be able to place orders.
2.  **Dashboard/Main View:** After login, the user should primarily see the combined order book for a selected trading pair (e.g., BTC/USDT).
3.  **Order Book Display:**
    *   Clearly distinguish between bids (buy orders) and asks (sell orders).
    *   Show the top 5 price levels for bids and asks.
    *   For each price level, display the total quantity available. This quantity is a sum of user orders at that price and external exchange orders at that price.
    *   The order book should update in real-time via WebSockets.
4.  **Placing Orders (User's Own Orders):**
    *   Users can place their own BUY or SELL limit orders for the selected trading pair.
    *   These orders are internal to this platform and are not sent to the external exchange.
    *   When a user places an order, it should be reflected in the combined order book if it falls within the top 5 levels.
5.  **Viewing Own Orders:** Users should be able to see a list of their own active internal orders, with options to cancel them.
6.  **Trading Pair Selection:** (Future enhancement, initially can be fixed) Users might be able to select different trading pairs to view.

## 4. User Experience Goals

- **Modern & Responsive:** The interface should be clean, intuitive, and adapt well to different screen sizes.
- **Real-time Feel:** Data updates should be quick and seamless, providing a sense of a live market.
- **Clarity:** Information (prices, quantities, order types) should be presented clearly and unambiguously.
- **Simplicity:** Avoid clutter and overly complex features not core to the order book and personal order management.
- **Feedback:** The system should provide clear feedback for actions (e.g., order placed, order cancelled, errors).
