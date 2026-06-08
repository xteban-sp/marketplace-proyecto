import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReviewService } from '../../core/services/review.service';
import { ProductService } from '../../core/services/product.service';
import { Review, ReviewRequest } from '../../core/models/review.model';
import { Product } from '../../core/models/product.model';

@Component({
  selector: 'app-resenas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './resenas.component.html'
})
export class ResenasComponent implements OnInit {
  private reviewService = inject(ReviewService);
  private productService = inject(ProductService);

  resenas = signal<Review[]>([]);
  productos = signal<Product[]>([]);
  loading = signal(false);
  enviando = signal(false);
  error = signal('');
  successMsg = signal('');

  form: ReviewRequest = {
    productoId: 0,
    usuarioId: 1,
    calificacion: 5,
    comentario: ''
  };

  ngOnInit(): void {
    this.cargarResenas();
    this.productService.getAll().subscribe({
      next: (data) => this.productos.set(data),
      error: () => {}
    });
  }

  cargarResenas(): void {
    this.loading.set(true);
    this.reviewService.getAll().subscribe({
      next: (data) => {
        this.resenas.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar las reseñas.');
        this.loading.set(false);
      }
    });
  }

  enviarResena(): void {
    if (!this.form.productoId || !this.form.comentario.trim()) {
      this.error.set('Completa todos los campos.');
      return;
    }
    this.enviando.set(true);
    this.error.set('');
    this.reviewService.create(this.form).subscribe({
      next: () => {
        this.successMsg.set('✅ Reseña enviada correctamente.');
        this.form = { productoId: 0, usuarioId: 1, calificacion: 5, comentario: '' };
        this.enviando.set(false);
        this.cargarResenas();
      },
      error: () => {
        this.error.set('No se pudo enviar la reseña.');
        this.enviando.set(false);
      }
    });
  }

  estrellas(n: number): string {
    return '★'.repeat(n) + '☆'.repeat(5 - n);
  }
}
