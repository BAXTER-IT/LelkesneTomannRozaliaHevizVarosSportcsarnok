import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router'; // Added RouterLink import

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink], // Added RouterLink to imports
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected title = 'Market Exchange'; // Updated title
}
