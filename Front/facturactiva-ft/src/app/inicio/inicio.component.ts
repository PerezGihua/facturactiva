import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './inicio.component.html',
  styleUrls: ['./inicio.component.css']
})
export class InicioComponent implements OnInit {

  nombreUser: string = '';
  userRole: string = '';

  constructor(private router: Router) {}

  ngOnInit() {

    if (typeof window !== 'undefined') {

      this.nombreUser = localStorage.getItem('nombreUser') || 'Usuario';
      const idRol = localStorage.getItem('idRol');

      switch (idRol) {
        case '1':
          this.userRole = 'Cliente';
          break;
        case '2':
          this.userRole = 'Jefe de Soporte';
          break;
        case '3':
          this.userRole = 'Agente de Soporte';
          break;
        default:
          this.userRole = 'Usuario';
          break;
      }

    }

  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

}
