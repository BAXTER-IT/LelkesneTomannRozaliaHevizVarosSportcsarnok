import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { AuthService } from './services/auth'; // Import AuthService
import { CommonModule } from '@angular/common'; // Import CommonModule for *ngIf

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, CommonModule], // Add CommonModule
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected title = 'Market Exchange';
  authService = inject(AuthService); // Inject AuthService

  logout(): void {
    this.authService.logout();
    // Navigation to /login is handled by AuthService.logout()
  }
}
