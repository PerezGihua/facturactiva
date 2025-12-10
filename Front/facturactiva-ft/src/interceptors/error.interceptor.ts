import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface ErrorResponse {
  code?: string;
  error?: string;
  message: string;
  timestamp?: string;
  status: number;
}

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor() {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // No procesar errores de login - dejarlos pasar al componente
    if (request.url.includes('/auth/login')) {
      return next.handle(request);
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Ocurrió un error desconocido';
        let errorObj: ErrorResponse | any = { status: error.status };

        if (error.error instanceof ErrorEvent) {
          // Error del cliente
          errorMessage = error.error.message;
        } else {
          // Error del servidor - extraer el mensaje del body
          if (error.error?.message) {
            errorMessage = error.error.message;
            errorObj = error.error;
          } else if (error.error?.error) {
            errorMessage = error.error.error;
            errorObj = error.error;
          } else if (error.statusText) {
            errorMessage = error.statusText;
          }
        }

        console.error('Error:', errorMessage, errorObj);
        return throwError(() => ({ 
          message: errorMessage, 
          status: error.status,
          error: errorObj
        }));
      })
    );
  }
}