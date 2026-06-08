import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../core/services/order.service';
import { Order } from '../../core/models/order.model';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pedidos.component.html'
})
export class PedidosComponent implements OnInit {
  private orderService = inject(OrderService);

  pedidos = signal<Order[]>([]);
  loading = signal(false);
  error = signal('');

  ngOnInit(): void {
    this.cargarPedidos();
  }

  cargarPedidos(): void {
    this.loading.set(true);
    this.error.set('');
    this.orderService.getAll().subscribe({
      next: (data) => {
        this.pedidos.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los pedidos.');
        this.loading.set(false);
      }
    });
  }

  estadoBadge(estado: string): string {
    const map: Record<string, string> = {
      PENDIENTE: 'badge-warning',
      CONFIRMADO: 'badge-info',
      ENVIADO: 'badge-info',
      ENTREGADO: 'badge-success',
      CANCELADO: 'badge-danger'
    };
    return map[estado] ?? 'badge-info';
  }
}
