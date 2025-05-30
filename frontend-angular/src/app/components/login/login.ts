import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, UserLoginResponseDTO } from '../../services/auth'; // Correct path and import DTO
import { OrderService } from '../../services/order'; // Import OrderService
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  imports: [FormsModule, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  username = '';
  password = '';
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  private authService = inject(AuthService);
  private orderService = inject(OrderService); // Inject OrderService
  private router = inject(Router);

  async login(): Promise<void> {
    if (!this.username || !this.password) {
      this.errorMessage.set('Username and password are required.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.authService.login(this.username, this.password).subscribe({
      next: (response: UserLoginResponseDTO | null) => {
        this.isLoading.set(false);
        if (response && response.username) {
          this.router.navigate(['/app/order-book']); // Navigate to a protected route on success
          
          // After successful login and navigation, fetch user's orders
          this.orderService.getMyOrders().subscribe({
            next: (orders) => {
              console.log('My orders fetched successfully after login:', orders);
              // Optionally, update a signal or service with these orders
              // if they need to be displayed immediately or cached.
            },
            error: (ordersError) => {
              console.error('Failed to fetch my-orders after login:', ordersError);
              // Handle error fetching orders, e.g., display a non-critical notification
            }
          });
        } else {
          this.errorMessage.set('Login failed. Please check your credentials.');
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set('An error occurred during login. Please try again.');
        console.error('Login error:', err);
      }
    });
  }
}
