import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../core/services/product.service';
import { OrderService } from '../../core/services/order.service';
import { Product } from '../../core/models/product.model';
import { OrderItem } from '../../core/models/order.model';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './productos.component.html'
})
export class ProductosComponent implements OnInit {
  private productService = inject(ProductService);
  private orderService = inject(OrderService);

  productos = signal<Product[]>([]);
  carrito = signal<(Product & { cantidad: number })[]>([]);
  loading = signal(false);
  loadingPedido = signal(false);
  error = signal('');
  successMsg = signal('');

  ngOnInit(): void {
    this.cargarProductos();
  }

  cargarProductos(): void {
    this.loading.set(true);
    this.error.set('');
    this.productService.getAll().subscribe({
      next: (data) => {
        this.productos.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los productos. Verifica que el backend está corriendo.');
        this.loading.set(false);
      }
    });
  }

  agregarAlCarrito(producto: Product): void {
    const actual = this.carrito();
    const existente = actual.find(p => p.id === producto.id);
    if (existente) {
      this.carrito.set(actual.map(p =>
        p.id === producto.id ? { ...p, cantidad: p.cantidad + 1 } : p
      ));
    } else {
      this.carrito.set([...actual, { ...producto, cantidad: 1 }]);
    }
  }

  quitarDelCarrito(id: number): void {
    this.carrito.set(this.carrito().filter(p => p.id !== id));
  }

  get totalCarrito(): number {
    return this.carrito().reduce((sum, p) => sum + p.precio * p.cantidad, 0);
  }

  realizarPedido(): void {
    if (!this.carrito().length) return;
    this.loadingPedido.set(true);
    this.error.set('');
    this.successMsg.set('');

    const items: OrderItem[] = this.carrito().map(p => ({
      productoId: p.id,
      cantidad: p.cantidad,
      precioUnitario: p.precio
    }));

    this.orderService.create({ usuarioId: 1, items }).subscribe({
      next: (pedido) => {
        this.successMsg.set(`✅ Pedido #${pedido.id} registrado correctamente.`);
        this.carrito.set([]);
        this.loadingPedido.set(false);
      },
      error: () => {
        this.error.set('No se pudo registrar el pedido. Intenta nuevamente.');
        this.loadingPedido.set(false);
      }
    });
  }
}
