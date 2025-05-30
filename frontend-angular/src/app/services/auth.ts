import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

// DTO for login response from backend
export interface UserLoginResponseDTO {
  username: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private baseUrl = ''; // Base URL will be relative to the proxy
  private readonly AUTH_KEY = 'currentUserAuth';

  // Using a signal for reactive authentication state
  public isAuthenticated = signal<boolean>(this.hasAuthData());
  public currentUser = signal<string | null>(this.getUsernameFromStorage());

  private http = inject(HttpClient);
  private router = inject(Router);

  constructor() {}

  private hasAuthData(): boolean {
    return typeof localStorage !== 'undefined' && !!localStorage.getItem(this.AUTH_KEY);
  }

  private getUsernameFromStorage(): string | null {
    if (typeof localStorage === 'undefined') return null;
    const authData = localStorage.getItem(this.AUTH_KEY);
    if (!authData) return null;
    try {
      const storedUser = JSON.parse(authData);
      return storedUser.username || null;
    } catch (e) {
      return null;
    }
  }

  // Updated Login Method
  login(username: string, password: string): Observable<UserLoginResponseDTO | null> {
    const headers = new HttpHeaders({
      Authorization: 'Basic ' + btoa(username + ':' + password),
      'Content-Type': 'application/json' // Good practice to set content type
    });

    return this.http.post<UserLoginResponseDTO>(`/api/auth/login`, {}, { headers }) // Relative path
      .pipe(
        tap(response => {
          // On successful login, response is UserLoginResponseDTO
          if (typeof localStorage !== 'undefined') {
            localStorage.setItem(this.AUTH_KEY, JSON.stringify({ username: response.username }));
          }
          this.isAuthenticated.set(true);
          this.currentUser.set(response.username);
        }),
        catchError(error => {
          this.logout(); // Clear any partial auth state on error
          return of(null); // Return null or an error object on failure
        })
      );
  }

  logout(): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(this.AUTH_KEY);
    }
    this.isAuthenticated.set(false);
    this.currentUser.set(null);
    this.router.navigate(['/login']); // Navigate to login
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
