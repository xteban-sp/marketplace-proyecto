import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Order, OrderRequest } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  getAll(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.base}/pedidos`);
  }

  getByUser(usuarioId: number): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.base}/pedidos?usuarioId=${usuarioId}`);
  }

  create(order: OrderRequest): Observable<Order> {
    return this.http.post<Order>(`${this.base}/pedidos`, order);
  }
}
