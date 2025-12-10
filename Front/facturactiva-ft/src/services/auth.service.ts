import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  idRol: number;
  nombreCompleto: string;
  email: string;
  message: string;
}

export interface AuthUser {
  email: string;
  idRol: number;
  nombreCompleto: string;
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth/login';
  private logoutUrl = 'http://localhost:8080/api/auth/logout';
  private currentUserSubject = new BehaviorSubject<AuthUser | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.initializeUser();
  }

  /**
   * Inicializa el usuario actual desde localStorage si existe
   */
  private initializeUser(): void {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('token');
      const email = localStorage.getItem('email');
      const idRol = localStorage.getItem('idRol');
      const nombreCompleto = localStorage.getItem('nombreCompleto');

      if (token && email && idRol && nombreCompleto) {
        this.currentUserSubject.next({
          token,
          email,
          idRol: parseInt(idRol, 10),
          nombreCompleto
        });
      }
    }
  }

  /**
   * Realiza el login con las credenciales proporcionadas
   */
  login(email: string, password: string): Observable<LoginResponse> {
    const loginData: LoginRequest = { email, password };

    return this.http.post<LoginResponse>(this.apiUrl, loginData).pipe(
      map((response) => {
        if (response.message === 'Autenticación exitosa') {
          this.storeAuthData(response);
          this.currentUserSubject.next({
            token: response.token,
            email: response.email,
            idRol: response.idRol,
            nombreCompleto: response.nombreCompleto
          });
        }
        return response;
      })
    );
  }

  /**
   * Guarda los datos de autenticación en localStorage
   */
  private storeAuthData(response: LoginResponse): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('token', response.token);
      localStorage.setItem('tokenType', response.tokenType);
      localStorage.setItem('email', response.email);
      localStorage.setItem('idRol', response.idRol.toString());
      localStorage.setItem('nombreCompleto', response.nombreCompleto);
    }
  }

  /**
   * Obtiene el token JWT actual
   */
  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('token');
    }
    return null;
  }

  /**
   * Verifica si el usuario está autenticado
   */
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  /**
   * Obtiene el usuario actual
   */
  getCurrentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  /**
   * Realiza el logout llamando al endpoint del backend
   */
  logout(): Observable<any> {
    return this.http.post<any>(this.logoutUrl, {}).pipe(
      map((response) => {
        console.log('Logout response:', response);
        this.clearAuthData();
        return response;
      })
    );
  }

  /**
   * Limpia los datos de autenticación
   */
  private clearAuthData(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('token');
      localStorage.removeItem('tokenType');
      localStorage.removeItem('email');
      localStorage.removeItem('idRol');
      localStorage.removeItem('nombreCompleto');
    }
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  /**
   * Fuerza el logout sin llamar al backend (útil para errores 401)
   */
  forceLogout(): void {
    this.clearAuthData();
  }
}
