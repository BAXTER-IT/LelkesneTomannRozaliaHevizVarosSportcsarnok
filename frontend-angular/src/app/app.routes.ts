import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { OrderBook } from './components/order-book/order-book';
import { OrderEntry } from './components/order-entry/order-entry';
import { MyOrders } from './components/my-orders/my-orders';
import { authGuard } from './guards/auth.guard'; // Import the authGuard

// A simple layout component for authenticated routes, if needed, or just use app.component
// For now, we'll assume app.component.html handles the main layout for authenticated views.

export const routes: Routes = [
  { path: 'login', component: Login },
  {
    path: 'app', // Main path for authenticated content
    canActivate: [authGuard],
    children: [
      { path: 'order-book', component: OrderBook },
      { path: 'my-orders', component: MyOrders },
      { path: 'trade', component: OrderEntry },
      { path: '', redirectTo: 'order-book', pathMatch: 'full' } // Default child route
    ]
  },
  // Redirect root to /app (which will be guarded) or /login based on auth status
  // The guard on /app will handle redirection to /login if not authenticated.
  // If authenticated, it's desirable to go to /app.
  // A resolver or another guard on the root path could handle this initial redirection logic,
  // or we can rely on the app component to do an initial check.
  // For simplicity, let's make the default path redirect to 'app'.
  // The authGuard on 'app' will then redirect to 'login' if necessary.
  { path: '', redirectTo: '/app', pathMatch: 'full' },
  { path: '**', redirectTo: '/app' } // Wildcard: redirect to app, guard handles the rest
];
