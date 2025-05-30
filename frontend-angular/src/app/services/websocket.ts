import { Injectable, OnDestroy } from '@angular/core';
import { Observable, Subject, Observer, Subscription } from 'rxjs';
import { webSocket, WebSocketSubject, WebSocketSubjectConfig } from 'rxjs/webSocket';
import { retryWhen, delay, tap } from 'rxjs/operators';

export interface WebSocketMessage {
  // Define the structure of messages you expect from the WebSocket
  // For example, for CombinedOrderBook:
  tradingPair?: string;
  bids?: { price: number; totalQuantity: number }[];
  asks?: { price: number; totalQuantity: number }[];
  timestamp?: number;
  type?: string; // e.g., 'orderBookUpdate', 'error', 'info'
  payload?: any;
}

@Injectable({
  providedIn: 'root',
})
export class WebSocketService implements OnDestroy {
  private socket$!: WebSocketSubject<WebSocketMessage>;
  private readonly wsUrl = 'ws://localhost:8080/ws/market'; // Adjust if your backend URL is different
  
  // Subject to allow components to subscribe to messages
  private messagesSubject = new Subject<WebSocketMessage>();
  public messages$: Observable<WebSocketMessage> = this.messagesSubject.asObservable();

  private connectionSubscription: Subscription | undefined;

  constructor() {
    // Consider connecting only when a component needs it, or if auth is required first.
    // For now, let's attempt connection on service instantiation.
    // this.connect(); 
  }

  public connect(): void {
    if (this.socket$ && !this.socket$.closed) {
      console.log('[WebSocketService] Already connected or connecting.');
      return;
    }

    console.log(`[WebSocketService] Attempting to connect to ${this.wsUrl}...`);
    const config: WebSocketSubjectConfig<WebSocketMessage> = {
      url: this.wsUrl,
      openObserver: {
        next: () => {
          console.log('[WebSocketService] Connection established successfully!');
          this.messagesSubject.next({ type: 'info', payload: 'WebSocket Connected' });
          // You could send an initial message if needed, e.g., to subscribe to specific topics/pairs
          // this.sendMessage({ action: 'subscribe', tradingPair: 'BTCUSDT' });
        },
      },
      closeObserver: {
        next: (closeEvent: CloseEvent) => { // Added type for closeEvent
          console.warn(`[WebSocketService] Connection closed. Code: ${closeEvent.code}, Reason: ${closeEvent.reason}, WasClean: ${closeEvent.wasClean}`);
          this.messagesSubject.next({ type: 'info', payload: `WebSocket Closed: ${closeEvent.reason || closeEvent.code}` });
          this.socket$ = null!; // Mark as closed
          // Potentially trigger reconnection logic here if desired
        },
      },
      // Deserializer for incoming messages (if they are strings)
      deserializer: (e: MessageEvent): WebSocketMessage => {
        console.log('[WebSocketService] Message received from server:', e.data);
        try {
          return JSON.parse(e.data);
        } catch (error) {
          console.error('[WebSocketService] Error parsing message:', error, 'Data:', e.data);
          // Return a structured error message
          return { type: 'error', payload: { error: 'ParseError', message: 'Invalid JSON received', data: e.data } };
        }
      },
      // Serializer for outgoing messages
      serializer: (value: any) => JSON.stringify(value),
    };

    this.socket$ = webSocket<WebSocketMessage>(config);

    this.connectionSubscription = this.socket$
      .pipe(
        tap({
          error: error => {
            console.error('[WebSocketService] Error in WebSocket stream (before retry):', error);
            this.messagesSubject.next({ type: 'error', payload: `WebSocket Error: ${error.message || 'Unknown error'}` });
          }
        }),
        retryWhen(errors =>
          errors.pipe(
            tap(err => console.warn('[WebSocketService] Attempting to reconnect WebSocket after error:', err)),
            delay(5000) // Retry after 5 seconds
          )
        )
      )
      .subscribe({
        next: (message) => {
          console.log('[WebSocketService] Forwarding message to subscribers:', message);
          this.messagesSubject.next(message);
        },
        error: (err) => {
          // This error handler is for unrecoverable errors after retries.
          console.error('[WebSocketService] Unrecoverable WebSocket error after retries:', err);
          this.messagesSubject.next({ type: 'error', payload: `Unrecoverable WebSocket Error: ${err.message || 'Connection failed after retries'}` });
          this.socket$ = null!; // Ensure socket is marked as unusable
        },
        complete: () => {
          console.log('[WebSocketService] WebSocket stream completed.');
        }
      });
  }

  public sendMessage(message: any): void {
    if (this.socket$ && !this.socket$.closed) {
      console.log('[WebSocketService] Sending message:', message);
      this.socket$.next(message);
    } else {
      console.warn('[WebSocketService] WebSocket not connected. Message not sent:', message);
      // Optionally, queue message or attempt to reconnect
    }
  }

  public closeConnection(): void {
    console.log('[WebSocketService] Closing connection explicitly.');
    if (this.connectionSubscription) {
      this.connectionSubscription.unsubscribe();
      this.connectionSubscription = undefined;
    }
    if (this.socket$) {
      this.socket$.complete(); // Closes the connection
      this.socket$ = null!; // Important to set to null after completion
      console.log('[WebSocketService] Connection explicitly closed by client.');
    }
  }

  ngOnDestroy(): void {
    this.closeConnection();
  }
}
