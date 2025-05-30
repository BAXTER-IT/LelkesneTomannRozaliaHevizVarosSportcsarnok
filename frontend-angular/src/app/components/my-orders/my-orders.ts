import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService, Order } from '../../services/order';
import { AuthService } from '../../services/auth';
import { Router } from '@angular/router';

@Component({
  selector: 'app-my-orders',
  imports: [CommonModule],
  templateUrl: './my-orders.html',
  styleUrl: './my-orders.scss'
})
export class MyOrders implements OnInit {
  myOrders = signal<Order[]>([]);
  isLoading = signal(true);
  errorMessage = signal<string | null>(null);

  private orderService = inject(OrderService);
  private authService = inject(AuthService);
  private router = inject(Router);

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadMyOrders();
  }

  loadMyOrders(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.orderService.getMyOrders().subscribe({
      next: (orders) => {
        this.myOrders.set(orders);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(`Failed to load orders: ${err.message || 'Unknown error'}`);
        console.error('Error loading my orders:', err);
      }
    });
  }

  cancelOrder(orderId: string | undefined): void {
    if (!orderId) {
      this.errorMessage.set('Order ID is missing, cannot cancel.');
      return;
    }
    if (!confirm('Are you sure you want to cancel this order?')) {
      return;
    }

    this.isLoading.set(true); // Or a specific cancelling state
    this.orderService.cancelOrder(orderId).subscribe({
      next: () => {
        this.isLoading.set(false);
        // Refresh the list of orders
        this.loadMyOrders(); 
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(`Failed to cancel order ${orderId}: ${err.message || 'Unknown error'}`);
        console.error(`Error cancelling order ${orderId}:`, err);
      }
    });
  }
}
