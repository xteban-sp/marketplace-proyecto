import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar">
      <a routerLink="/productos" class="brand">🛒 Marketplace</a>
      <div>
        <a routerLink="/productos" routerLinkActive="active">Productos</a>
        <a routerLink="/pedidos" routerLinkActive="active">Mis Pedidos</a>
        <a routerLink="/resenas" routerLinkActive="active">Reseñas</a>
      </div>
    </nav>
    <router-outlet />
  `
})
export class AppComponent {}
