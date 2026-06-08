export interface Review {
  id: number;
  productoId: number;
  usuarioId: number;
  calificacion: number;
  comentario: string;
  fechaCreacion?: string;
}

export interface ReviewRequest {
  productoId: number;
  usuarioId: number;
  calificacion: number;
  comentario: string;
}
