import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService, AuthUser } from '../../services/auth.service';
import { UserUtilsService } from '../../services/user-utils.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './inicio.component.html',
  styleUrls: ['./inicio.component.css']
})
export class InicioComponent implements OnInit {

  currentUser$: Observable<AuthUser | null>;
  isLoggingOut = false;

  constructor(
    private router: Router,
    private authService: AuthService,
    public utils: UserUtilsService
  ) {
    this.currentUser$ = this.authService.currentUser$;
  }

  ngOnInit() {
    // Los datos se obtienen directamente del AuthService
    // El Observable currentUser$ está disponible en el template
  }

  // ahora usamos UserUtilsService para helpers de usuario en la plantilla

  logout() {
    this.isLoggingOut = true;
    
    this.authService.logout().subscribe({
      next: (response) => {
        console.log('Logout exitoso:', response);
        this.isLoggingOut = false;
        // El logout() del servicio ya maneja la redirección
      },
      error: (err) => {
        console.error('Error en logout:', err);
        this.isLoggingOut = false;
        // Forzar logout incluso si hay error
        this.authService.forceLogout();
      }
    });
  }

}
