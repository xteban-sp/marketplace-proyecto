import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Review, ReviewRequest } from '../models/review.model';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  getByProduct(productoId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.base}/resenas?productoId=${productoId}`);
  }

  getAll(): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.base}/resenas`);
  }

  create(review: ReviewRequest): Observable<Review> {
    return this.http.post<Review>(`${this.base}/resenas`, review);
  }
}
