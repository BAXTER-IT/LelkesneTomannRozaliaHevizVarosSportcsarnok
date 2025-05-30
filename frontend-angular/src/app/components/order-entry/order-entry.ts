import { Component, inject, signal, Input, effect } from '@angular/core';
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

  orderTypeSelected = signal<'market' | 'limit'>('market');

  @Input() bestBidPrice: number | null = null;
  @Input() bestAskPrice: number | null = null;
  
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

    effect(() => {
      const currentOrderType = this.orderTypeSelected();
      const currentSide = this.orderRequest.type; // 'BUY' or 'SELL'

      if (currentOrderType === 'market') {
        if (currentSide === 'BUY' && this.bestAskPrice !== null) {
          this.orderRequest.price = this.bestAskPrice;
        } else if (currentSide === 'SELL' && this.bestBidPrice !== null) {
          this.orderRequest.price = this.bestBidPrice;
        } else {
          // Set to 0 or a non-submittable value if price isn't available
          // This also helps in the form validation for price > 0
          this.orderRequest.price = 0; 
        }
      } else {
        // For 'limit' orders, if switching from 'market',
        // we might want to clear the price or set it to a default.
        // For now, we'll let the user manage it.
        // If a previous market price was set, it will remain unless changed by user.
        // Consider this.orderRequest.price = 0; if a reset is desired.
      }
    });
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
