import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'appcrearticket-crearticket',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './crearticket.component.html',
  styleUrls: ['./crearticket.component.css']
})
export class CrearticketComponent implements OnInit {

  nombreUser: string = '';
  userRole: string = '';

  isPanelOpen = false;

  togglePanel() {
    this.isPanelOpen = !this.isPanelOpen;
  }

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

}
