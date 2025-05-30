import { Routes } from '@angular/router';
import { Login } from './components/login/login'; // Corrected
import { OrderBook } from './components/order-book/order-book'; // Corrected
import { OrderEntry } from './components/order-entry/order-entry'; // Corrected
import { MyOrders } from './components/my-orders/my-orders'; // Corrected
// import { authGuard } from './guards/auth.guard'; // To be created

export const routes: Routes = [
  { path: 'login', component: Login }, // Corrected
  { 
    path: 'order-book', 
    component: OrderBook, // Corrected
    // canActivate: [authGuard] // Protect this route
  },
  { 
    path: 'my-orders', 
    component: MyOrders, // Corrected
    // canActivate: [authGuard] 
  },
  { 
    path: 'trade', // This might be integrated into another component later
    component: OrderEntry, // Corrected
    // canActivate: [authGuard] 
  },
  { path: '', redirectTo: '/order-book', pathMatch: 'full' }, // Default route
  { path: '**', redirectTo: '/order-book' } // Wildcard route for a 404 or redirect
];
