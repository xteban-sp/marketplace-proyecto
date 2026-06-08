export interface OrderItem {
  productoId: number;
  cantidad: number;
  precioUnitario?: number;
}

export interface OrderRequest {
  usuarioId: number;
  items: OrderItem[];
}

export interface Order {
  id: number;
  usuarioId: number;
  estado: 'PENDIENTE' | 'CONFIRMADO' | 'ENVIADO' | 'ENTREGADO' | 'CANCELADO';
  total: number;
  fechaCreacion: string;
  items: OrderItem[];
}
