import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TicketsService } from '../../services/tickets.service';
import { AuthService, AuthUser } from '../../services/auth.service';
import { Observable } from 'rxjs';
import { UserUtilsService } from '../../services/user-utils.service';
// import { AuthService } from '../../services/auth.service';

interface Ticket {
  codigo: string;
  asunto: string;
  descripcion: string;
  numDocRechazado?: string;
  fechaCreacion: string;
  estado: string;
  tipoIcono: string;
  tipoComprobante?: string;
  idTicket?: number;
}

@Component({
  selector: 'app-tickets',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './tickets.component.html',
  styleUrls: ['./tickets.component.css']
})
export class TicketsComponent implements OnInit {

  nombreUser: string = '';
  userRole: string = '';
  searchTerm: string = '';
  currentUser$: Observable<AuthUser | null>;

  ticketsOriginales: Ticket[] = [
    {
      codigo: 'FA-314',
      asunto: 'Factura no aceptada',
      descripcion: 'Tengo una factura rechazada, la cual necesito s...',
      fechaCreacion: '09/08/2025',
      estado: 'En Progreso',
      tipoIcono: 'fa-file-invoice',
      tipoComprobante: 'factura',
      idTicket: 314
    },
    {
      codigo: 'FA-261',
      asunto: 'Boleta Rechazada',
      descripcion: 'Tengo una boleta rechazada, la cual necesito s...',
      fechaCreacion: '27/03/2025',
      estado: 'Por Hacer',
      tipoIcono: 'fa-receipt',
      tipoComprobante: 'boleta',
      idTicket: 261
    },
    {
      codigo: 'FA-120',
      asunto: 'Factura Rechazada',
      descripcion: 'Tengo una factura rechazada, la cual necesito s...',
      fechaCreacion: '17/05/2024',
      estado: 'Finalizado',
      tipoIcono: 'fa-file-invoice',
      tipoComprobante: 'factura',
      idTicket: 120
    }
  ];

  tickets: Ticket[] = [];
  isLoadingTickets = false;
  loadErrorMessage: string = '';

  constructor(public router: Router, private route: ActivatedRoute, private ticketsService: TicketsService, private authService: AuthService, public utils: UserUtilsService) {
    this.currentUser$ = this.authService.currentUser$;
  }

  ngOnInit() {
    this.tickets = [...this.ticketsOriginales];

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
    // Intentar cargar tickets reales desde el backend
    this.loadMyTickets();
  }

  /**
   * Carga los tickets del backend cuando existe conexión
   */
  loadMyTickets() {
    this.isLoadingTickets = true;
    this.loadErrorMessage = '';

    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
    this.ticketsService.getMyTickets(token || undefined).subscribe({
      next: (res) => {
        try {
          const mapped = res.map(item => ({
            codigo: item.idTicket ? this.formatTicketCode(item.idTicket, item.tipoComprobante) : (item.numDocRechazado || 'N/A'),
            asunto: item.asunto,
            descripcion: item.descripcion,
            numDocRechazado: item.numDocRechazado || '',
            fechaCreacion: this.formatDate(item.fechaCreacion),
            estado: this.mapEstado(item.estado),
            tipoIcono: this.mapTipoIcono(item.tipoComprobante),
            tipoComprobante: item.tipoComprobante || ''
          } as Ticket));

          this.ticketsOriginales = mapped;
          // ordenar por id ascendente
          this.sortTicketsAscending();
          this.tickets = [...this.ticketsOriginales];
        } catch (e) {
          console.error('Error mapeando tickets:', e);
          this.loadErrorMessage = 'Error procesando datos de tickets.';
        } finally {
          this.isLoadingTickets = false;
        }
      },
      error: (err) => {
        console.error('No se pudo cargar tickets desde backend, usando datos locales.', err);
        this.loadErrorMessage = err?.error?.message || 'No se pudo cargar sus tickets. Intente nuevamente.';
        this.isLoadingTickets = false;
        // keep static ticketsOriginales
      }
    });
  }

  /**
   * Devuelve el valor numérico del id de un ticket.
   * Prioriza la propiedad `idTicket`, si no existe intenta extraer el número del `codigo`.
   */
  private getNumericId(ticket: Ticket): number {
    if (typeof ticket.idTicket === 'number') return ticket.idTicket;
    if (ticket.codigo) {
      const m = ticket.codigo.match(/(\d+)/);
      if (m) return parseInt(m[1], 10) || Number.POSITIVE_INFINITY;
    }
    return Number.POSITIVE_INFINITY;
  }

  /**
   * Ordena `ticketsOriginales` de menor a mayor por id numérico y actualiza el arreglo.
   */
  private sortTicketsAscending() {
    this.ticketsOriginales.sort((a, b) => this.getNumericId(a) - this.getNumericId(b));
  }

  private mapTipoIcono(tipo: string): string {
    if (!tipo) return 'fa-ticket';
    const t = tipo.toLowerCase();
    if (t.includes('factura')) return 'fa-file-invoice';
    if (t.includes('boleta') || t.includes('boleta de venta')) return 'fa-receipt';
    return 'fa-ticket';
  }

  /**
   * Formatea idTicket a un código con prefijo según tipo de comprobante.
   * Ej: (1, 'boleta') -> BXA-001 ; (1, 'factura') -> FXA-001
   */
  private formatTicketCode(idTicket: number | string, tipoComprobante?: string): string {
    const n = typeof idTicket === 'number' ? idTicket : parseInt(idTicket as string, 10) || 0;
    const t = (tipoComprobante || '').toLowerCase();
    const prefix = t.includes('boleta') ? 'BXA' : 'FXA';
    return `${prefix}-${n.toString().padStart(3, '0')}`;
  }

  /**
   * Devuelve el color del icono según el tipo de comprobante.
   */
  getIconColor(tipoComprobante?: string): string {
    const t = (tipoComprobante || '').toLowerCase();
    if (t.includes('boleta')) return '#f1c40f'; // amarillo
    if (t.includes('factura')) return '#007bff'; // azul
    // fallback a color por defecto que estaba en CSS
    return '#ffc107';
  }

  /**
   * Formatea fecha ISO a DD/MM/YYYY
   */
  private formatDate(dateStr?: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return '';
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const yyyy = d.getFullYear();
    return `${dd}/${mm}/${yyyy}`;
  }

  private mapEstado(nombreEstado: string): string {
    if (!nombreEstado) return 'Por Hacer';
    const n = nombreEstado.toLowerCase();
    if (n.includes('asign') || n.includes('en progreso')) return 'En Progreso';
    if (n.includes('nuevo') || n.includes('por hacer')) return 'Por Hacer';
    if (n.includes('final') || n.includes('cerrad')) return 'Finalizado';
    return nombreEstado;
  }

  // FILTRO EN TIEMPO REAL
  filtrarTickets() {
    if (!this.searchTerm.trim()) {
      this.tickets = [...this.ticketsOriginales];
      return;
    }

    const busqueda = this.searchTerm.toLowerCase();

    this.tickets = this.ticketsOriginales.filter(ticket => 
      ticket.codigo.toLowerCase().includes(busqueda) ||
      ticket.asunto.toLowerCase().includes(busqueda) ||
      ticket.descripcion.toLowerCase().includes(busqueda) ||
      ticket.fechaCreacion.toLowerCase().includes(busqueda) ||
      ticket.estado.toLowerCase().includes(busqueda)
    );
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