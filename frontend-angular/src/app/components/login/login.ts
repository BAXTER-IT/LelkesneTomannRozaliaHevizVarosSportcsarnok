import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms'; // Import FormsModule
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth'; // Correct path to AuthService
import { CommonModule } from '@angular/common'; // For @if directive

@Component({
  selector: 'app-login',
  imports: [FormsModule, CommonModule], // Add FormsModule and CommonModule
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  username = '';
  password = '';
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  private authService = inject(AuthService);
  private router = inject(Router);

  async login(): Promise<void> {
    if (!this.username || !this.password) {
      this.errorMessage.set('Username and password are required.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.authService.login(this.username, this.password).subscribe({
      next: (success) => {
        this.isLoading.set(false);
        if (success) {
          this.router.navigate(['/app/order-book']); // Navigate to a protected route on success
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
