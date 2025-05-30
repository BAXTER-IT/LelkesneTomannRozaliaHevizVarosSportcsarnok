import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, tap, BehaviorSubject } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private baseUrl = 'http://localhost:8080'; // Adjust if your backend URL is different
  private readonly AUTH_KEY = 'currentUserAuth';

  // Using a signal for reactive authentication state
  public isAuthenticated = signal<boolean>(this.hasAuthData());
  public currentUser = signal<string | null>(this.getUsernameFromStorage());

  constructor(private http: HttpClient) {}

  private hasAuthData(): boolean {
    return typeof localStorage !== 'undefined' && !!localStorage.getItem(this.AUTH_KEY);
  }

  private getUsernameFromStorage(): string | null {
    if (typeof localStorage === 'undefined') return null;
    const authData = localStorage.getItem(this.AUTH_KEY);
    if (!authData) return null;
    try {
      // Basic Auth: "Basic base64(username:password)"
      // We'll just store the username for simplicity, not the full token for security.
      // Or, if we switch to JWT, we'd store the JWT and decode username from it.
      // For now, let's assume we store username directly after successful basic auth.
      const storedUser = JSON.parse(authData);
      return storedUser.username || null;
    } catch (e) {
      return null;
    }
  }

  // Basic Authentication Login
  login(username: string, password: string): Observable<boolean> {
    // For Basic Auth, the "token" is 'Basic ' + base64(username + ':' + password)
    // We don't actually send a /login request for basic auth,
    // the browser handles sending the Authorization header on subsequent requests.
    // This login method will just verify credentials by making a simple authenticated request.
    // A common approach is to fetch user details or a protected resource.
    // Let's try to fetch user's orders as a way to verify.
    const headers = new HttpHeaders({
      Authorization: 'Basic ' + btoa(username + ':' + password),
    });

    return this.http.get<any[]>(`${this.baseUrl}/api/orders/my-orders`, { headers, observe: 'response' })
      .pipe(
        tap(response => {
          if (response.ok) {
            // Store some indication of auth, e.g., the username and a flag or the basic token
            // For simplicity, storing username. In a real app with JWT, you'd store the JWT.
            if (typeof localStorage !== 'undefined') {
              localStorage.setItem(this.AUTH_KEY, JSON.stringify({ username }));
            }
            this.isAuthenticated.set(true);
            this.currentUser.set(username);
          }
        }),
        catchError(error => {
          this.logout(); // Clear any partial auth state on error
          return of(false);
        }),
        // Convert to boolean indicating success
        (obs: Observable<any>) => obs.pipe(tap(res => !!res.ok), catchError(() => of(false)))
      ) as Observable<boolean>;
  }

  logout(): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(this.AUTH_KEY);
    }
    this.isAuthenticated.set(false);
    this.currentUser.set(null);
    // Optionally, notify backend if session management is involved there
  }

  // Helper to get auth headers for other services
  getAuthHeaders(): HttpHeaders | null {
    if (!this.isAuthenticated() || typeof localStorage === 'undefined') {
      return null;
    }
    // For Basic Auth, we need to reconstruct it or store it.
    // This is a simplification; typically with Basic Auth, you don't store the password.
    // For a real app, we'd switch to JWTs.
    // For now, let's assume other services will handle their own auth if needed,
    // or we'd need a more secure way to re-generate/store the basic token.
    // A better approach for basic auth is to let the browser handle it after first challenge,
    // or use a dedicated login endpoint that establishes a session.
    // Given our current backend, we'll need to provide the basic auth header for each request.
    // This implies we'd need to temporarily hold credentials or the token, which is not ideal for basic.
    // Let's assume for now that login sets up the browser's basic auth context,
    // or we'll refine this when implementing components.
    // For services that need it, they might have to ask for credentials again or use a stored token.
    // This is a placeholder, as Basic Auth is usually handled by the browser automatically
    // after the first 401 challenge, or by including the header in every request.
    // If we stored the base64 token:
    // const authData = localStorage.getItem(this.AUTH_KEY);
    // if (authData) return new HttpHeaders({ Authorization: authData });
    return null; // Placeholder - will be improved with JWT or by services constructing their own.
  }
}
