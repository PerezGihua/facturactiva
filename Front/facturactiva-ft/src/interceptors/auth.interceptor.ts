import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // No agregar token a la request de login - dejar pasar directamente
    if (request.url.includes('/auth/login')) {
      console.log('AuthInterceptor: Request de login, no se agrega token');
      return next.handle(request);
    }

    // Agregar token JWT a las demás requests
    if (isPlatformBrowser(this.platformId)) {
      const token = this.authService.getToken();
      console.log('AuthInterceptor: Token obtenido:', token ? 'Existe (longitud: ' + token.length + ')' : 'No existe');

      if (token) {
        request = request.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        });
        console.log('AuthInterceptor: Header Authorization agregado a', request.url);
      } else {
        console.warn('AuthInterceptor: No hay token disponible para', request.url);
      }
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        console.error('AuthInterceptor: Error interceptado:', error.status, error.message);

        // Si el token es inválido o expiró (401), hacer logout
        if (error.status === 401) {
          console.warn('AuthInterceptor: Error 401, forzando logout');
          this.authService.forceLogout();
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      })
    );
  }
}
