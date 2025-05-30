import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common'; // For async pipe and ngFor/ngIf
import { Subscription } from 'rxjs';
import { OrderEntry } from '../order-entry/order-entry'; // Import OrderEntryComponent
import { WebSocketService, WebSocketMessage } from '../../services/websocket';
import { AuthService } from '../../services/auth';
import { Router } from '@angular/router';

export interface DisplayOrderBookEntry {
  price: number;
  totalQuantity: number;
  source: 'USER' | 'BINANCE'; // <-- New field
}

@Component({
  selector: 'app-order-book',
  imports: [CommonModule, OrderEntry], // Add OrderEntryComponent to imports
  templateUrl: './order-book.html',
  styleUrl: './order-book.scss'
})
export class OrderBook implements OnInit, OnDestroy {
  private webSocketService = inject(WebSocketService);
  private authService = inject(AuthService);
  private router = inject(Router);

  private messageSubscription: Subscription | undefined;

  // Signals for bids and asks
  bids = signal<DisplayOrderBookEntry[]>([]);
  asks = signal<DisplayOrderBookEntry[]>([]);
  currentTradingPair = signal<string | null>(null);
  lastUpdated = signal<Date | null>(null);

  isLoading = signal(true);
  errorMessage = signal<string | null>(null);
  showOrderEntryForm = signal(false); // Signal to control form visibility

  toggleOrderEntryForm(): void {
    this.showOrderEntryForm.set(!this.showOrderEntryForm());
  }

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    this.webSocketService.connect(); // Ensure connection is active
    this.messageSubscription = this.webSocketService.messages$.subscribe({
      next: (message: WebSocketMessage) => {
        this.isLoading.set(false);
        if (message.type === 'error') {
          this.errorMessage.set(message.payload || 'Error receiving data.');
          console.error('OrderBook WebSocket error message:', message);
        } else if (message.bids && message.asks && message.tradingPair) {
          // Assuming the message is a CombinedOrderBook
          this.currentTradingPair.set(message.tradingPair);
          // Ensure the incoming bids/asks are correctly cast to DisplayOrderBookEntry[]
          // which now includes the 'source' property.
          this.bids.set(message.bids as DisplayOrderBookEntry[]);
          this.asks.set(message.asks as DisplayOrderBookEntry[]);
          if (message.timestamp) {
            this.lastUpdated.set(new Date(message.timestamp));
          }
          this.errorMessage.set(null);
          console.log('Order book update received:', message);
        } else {
          console.log('Received WebSocket message:', message);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set('Failed to receive order book updates.');
        console.error('OrderBook WebSocket subscription error:', err);
      }
    });

    // Optionally, send a message to subscribe to a specific pair if backend requires it
    // this.webSocketService.sendMessage({ action: 'subscribe', tradingPair: 'BTCUSDT' });
  }

  ngOnDestroy(): void {
    if (this.messageSubscription) {
      this.messageSubscription.unsubscribe();
    }
    // Optionally close WebSocket connection if no other component uses it,
    // or manage connection lifecycle more globally.
    // this.webSocketService.closeConnection(); 
  }
}
