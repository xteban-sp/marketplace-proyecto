import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'productos', pathMatch: 'full' },
  {
    path: 'productos',
    loadComponent: () =>
      import('./features/productos/productos.component').then(m => m.ProductosComponent)
  },
  {
    path: 'pedidos',
    loadComponent: () =>
      import('./features/pedidos/pedidos.component').then(m => m.PedidosComponent)
  },
  {
    path: 'resenas',
    loadComponent: () =>
      import('./features/resenas/resenas.component').then(m => m.ResenasComponent)
  },
  { path: '**', redirectTo: 'productos' }
];
