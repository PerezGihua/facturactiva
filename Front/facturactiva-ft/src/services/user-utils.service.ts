import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UserUtilsService {
  constructor() {}

  /**
   * Extrae el primer nombre y primer apellido del nombre completo
   */
  getShortName(nombreCompleto: string): string {
    if (!nombreCompleto) return '';

    const parts = nombreCompleto.trim().split(/\s+/);

    if (parts.length === 0) return '';
    if (parts.length === 1) return parts[0];
    if (parts.length === 2) return `${parts[0]} ${parts[1]}`;

    const first = parts[0];
    const lastTwo = parts.slice(-2).join(' ');
    return `${first} ${lastTwo}`;
  }

  /**
   * Retorna el rol basado en el ID
   */
  getRolName(idRol: number): string {
    switch (idRol) {
      case 1:
        return 'Cliente';
      case 2:
        return 'Jefe de Soporte';
      case 3:
        return 'Agente de Soporte';
      default:
        return 'Usuario';
    }
  }
}
