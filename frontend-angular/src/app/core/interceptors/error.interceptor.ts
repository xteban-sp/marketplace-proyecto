import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

/**
 * Interceptor global de errores HTTP.
 * Registra en consola y re-lanza para que cada componente
 * pueda mostrar su propio mensaje de error.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error(`[HTTP Error] ${req.method} ${req.url}`, error.status, error.message);
      return throwError(() => error);
    })
  );
};
