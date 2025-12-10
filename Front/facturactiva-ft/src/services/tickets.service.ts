import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TicketsService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Obtiene los tickets del usuario. Si se pasa `token`, lo añade al header.
   */
  getMyTickets(token?: string): Observable<any[]> {
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return this.http.get<any[]>(`${this.baseUrl}/tickets/mis-tickets`, { headers });
  }

  /**
   * Crea un nuevo ticket con FormData (documento, asunto, tipo, descripcion, archivo)
   * El interceptor agregará automáticamente el token de autorización
   */
  createTicket(formData: FormData): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/tickets`, formData);
  }
}
