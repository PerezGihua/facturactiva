import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

interface Ticket {
  codigo: string;
  asunto: string;
  descripcion: string;
  fechaCreacion: string;
  estado: string;
  tipoIcono: string;
}

@Component({
  selector: 'app-tickets',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './tickets.component.html',
  styleUrls: ['./tickets.component.css']
})
export class TicketsComponent implements OnInit {

  nombreUser: string = '';
  userRole: string = '';

  tickets: Ticket[] = [
    {
      codigo: 'FA-314',
      asunto: 'Factura no aceptada',
      descripcion: 'Tengo una factura rechazada, la cual necesito s...',
      fechaCreacion: '09/08/2025',
      estado: 'En Progreso',
      tipoIcono: 'fa-file-invoice'
    },
    {
      codigo: 'FA-261',
      asunto: 'Boleta Rechazada',
      descripcion: 'Tengo una boleta rechazada, la cual necesito s...',
      fechaCreacion: '27/03/2025',
      estado: 'Por Hacer',
      tipoIcono: 'fa-receipt'
    },
    {
      codigo: 'FA-120',
      asunto: 'Factura Rechazada',
      descripcion: 'Tengo una factura rechazada, la cual necesito s...',
      fechaCreacion: '17/05/2024',
      estado: 'Finalizado',
      tipoIcono: 'fa-file-invoice'
    }
  ];

 
  constructor(public router: Router, private route: ActivatedRoute) {}

  ngOnInit() {

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

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  editarTicket(codigo: string) {
    this.router.navigate(['editar', codigo], { relativeTo: this.route });
  }

  eliminarTicket(codigo: string) {
    console.log('Eliminar ticket:', codigo);
  }

  getEstadoClass(estado: string): string {

    switch (estado) {
      case 'En Progreso':
        return 'estado-progreso';
      case 'Por Hacer':
        return 'estado-por-hacer';
      case 'Finalizado':
        return 'estado-finalizado';
      default:
        return '';
    }

  }

}
