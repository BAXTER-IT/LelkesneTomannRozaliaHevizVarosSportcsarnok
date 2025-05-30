import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from './auth'; // Assuming AuthService is in the same directory

// Define interfaces for order data, consistent with backend models
export interface Order {
  orderId?: string;
  userId?: string;
  type: 'BUY' | 'SELL';
  price: number;
  quantity: number;
  timestamp?: string; // ISO string date
  source?: 'USER' | 'BINANCE';
  tradingPair: string;
}

export interface OrderCreateRequest {
  type: 'BUY' | 'SELL';
  price: number;
  quantity: number;
  tradingPair: string;
}


@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private baseUrl = '/api/orders'; // Relative path for proxy

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getAuthHeaders(): HttpHeaders {
    // This is a simplified way to handle Basic Auth for each request.
    // In a real app, especially with JWT, this would involve retrieving a stored token.
    // For Basic Auth, if not handled by the browser automatically after initial challenge,
    // we'd need the current user's credentials. This is not ideal.
    // The AuthService's login method currently stores username, not credentials or full token.
    // This highlights a point for future improvement (e.g., move to JWT).
    // For now, we'll assume that if a user is "logged in" (via AuthService.login),
    // we need to reconstruct the Basic Auth header. This requires storing credentials
    // or the base64 token, which AuthService currently doesn't do securely.
    
    // TEMPORARY/SIMPLIFIED: Re-prompt or use dummy for now if not available.
    // This part needs a proper solution (e.g. JWT or secure token storage).
    // For the purpose of this example, if we had stored the base64 token during login:
    // const authToken = this.authService.getStoredAuthToken(); // Method to be added in AuthService
    // if (authToken) {
    //   return new HttpHeaders({
    //     'Content-Type': 'application/json',
    //     Authorization: authToken,
    //   });
    // }
    // Fallback for now - this won't work without actual credentials.
    // This part needs to be properly implemented based on how auth state/token is managed.
    // Let's assume for now that the browser's Basic Auth context is set,
    // or that we will implement a more robust token management in AuthService.
    // For testing with curl, we supplied user:pass. For HttpClient, it's similar.
    // If we rely on the AuthService to hold current credentials (NOT RECOMMENDED FOR PRODUCTION):
    // const currentUser = this.authService.currentUser(); // This is just username
    // This is a placeholder. Actual implementation depends on how login is fully handled.
    // For now, we'll construct it manually if a user is logged in, assuming we have a way to get credentials.
    // This is a significant simplification.
    const username = this.authService.currentUser();
    if (username) {
      // This is highly insecure and just for demonstration if we were to pass raw credentials
      // const password = "password_placeholder"; // We don't have the password here
      // return new HttpHeaders({
      //   'Content-Type': 'application/json',
      //   Authorization: 'Basic ' + btoa(username + ':' + password),
      // });
      // A better temporary approach for Basic Auth if the browser isn't caching:
      // The AuthService should store the `Authorization` header value itself upon successful login.
      const authData = localStorage.getItem('currentUserAuth'); // As stored by AuthService
      if (authData) {
        // This assumes 'currentUserAuth' stores the actual 'Basic base64token' or similar
        // The current AuthService stores {username: "user1"}, not the token.
        // This needs to be aligned. Let's assume AuthService.login stores the full header value.
        // For now, we'll just send Content-Type and rely on browser or future interceptor.
         return new HttpHeaders({ 'Content-Type': 'application/json' });
      }
    }
    return new HttpHeaders({ 'Content-Type': 'application/json' });
  }


  createOrder(orderRequest: OrderCreateRequest): Observable<Order> {
    // HttpClient handles Basic Auth if credentials are provided with `withCredentials: true`
    // and the server issues a 401 challenge, or if an interceptor adds the header.
    // For explicit Basic Auth header per request (if not handled by browser session):
    // This requires AuthService to provide the actual token.
    // For now, we'll rely on the browser's auth handling or an interceptor to be added later.
    return this.http.post<Order>(this.baseUrl, orderRequest, { 
        headers: this.getAuthHeaders(), // This needs to be correctly implemented
        withCredentials: true // Important for session-based or browser-handled Basic Auth
    })
      .pipe(catchError(this.handleError));
  }

  cancelOrder(orderId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${orderId}`, { 
        headers: this.getAuthHeaders(),
        withCredentials: true 
    })
      .pipe(catchError(this.handleError));
  }

  getMyOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.baseUrl}/my-orders`, { 
        headers: this.getAuthHeaders(),
        withCredentials: true 
    })
      .pipe(catchError(this.handleError));
  }

  private handleError(error: any): Observable<never> {
    console.error('An API error occurred', error);
    // Could transform error for user consumption
    return throwError(() => new Error('API Error: ' + (error.message || error.statusText || 'Unknown error')));
  }
}
