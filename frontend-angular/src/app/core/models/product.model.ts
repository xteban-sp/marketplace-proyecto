export interface Product {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  stock: number;
  categoriaId?: number;
  categoria?: string;
  imagenUrl?: string;
}

export interface Category {
  id: number;
  nombre: string;
}
