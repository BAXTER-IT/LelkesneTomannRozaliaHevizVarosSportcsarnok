import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { OrderService, OrderCreateRequest } from '../../services/order';
import { AuthService } from '../../services/auth';
import { Router } from '@angular/router';

@Component({
  selector: 'app-order-entry',
  imports: [FormsModule, CommonModule],
  templateUrl: './order-entry.html',
  styleUrl: './order-entry.scss'
})
export class OrderEntry {
  orderRequest: OrderCreateRequest = {
    type: 'BUY',
    price: 0,
    quantity: 0,
    tradingPair: 'BTCUSDT' // Default or allow selection
  };
  
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  private orderService = inject(OrderService);
  private authService = inject(AuthService);
  private router = inject(Router);

  constructor() {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
    }
  }

  submitOrder(): void {
    if (!this.orderRequest.tradingPair || this.orderRequest.price <= 0 || this.orderRequest.quantity <= 0) {
      this.errorMessage.set('Please fill in all fields with valid values.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.orderService.createOrder(this.orderRequest).subscribe({
      next: (createdOrder) => {
        this.isLoading.set(false);
        this.successMessage.set(`Order ${createdOrder.orderId} created successfully!`);
        // Reset form or navigate away
        this.orderRequest = { type: 'BUY', price: 0, quantity: 0, tradingPair: 'BTCUSDT' };
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(`Failed to create order: ${err.message || 'Unknown error'}`);
        console.error('Order creation error:', err);
      }
    });
  }
}
