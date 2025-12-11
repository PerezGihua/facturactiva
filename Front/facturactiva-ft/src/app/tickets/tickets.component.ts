import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TicketsService } from '../../services/tickets.service';
import { AuthService, AuthUser } from '../../services/auth.service';
import { Observable, Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { UserUtilsService } from '../../services/user-utils.service';
import { ModalticketComponent } from '../modalticket/modalticket.component'; // ⬅️ NUEVO IMPORT

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
  prioridad?: string;
  agente?: string;
  fechaUltimaActualizacion?: string;
  fechaCierre?: string;
  rutaArchivo?: string;
  nombreArchivo?: string;
}

@Component({
  selector: 'app-tickets',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule, ModalticketComponent], // ⬅️ AGREGADO ModalticketComponent
  templateUrl: './tickets.component.html',
  styleUrls: ['./tickets.component.css']
})
export class TicketsComponent implements OnInit, OnDestroy {

  nombreUser: string = '';
  userRole: string = '';
  idRol: string = '';
  searchTerm: string = '';
  currentUser$: Observable<AuthUser | null>;

  ticketsOriginales: Ticket[] = [];

  tickets: Ticket[] = [];
  isLoadingTickets = false;
  loadErrorMessage: string = '';

  // Modal de confirmación
  showModal = false;
  modalConfig = {
    title: '',
    message: '',
    type: 'confirm' as 'confirm' | 'alert',
    onConfirm: () => {},
    onCancel: () => {}
  };

  // Modal para agente de soporte
  mostrarModalAgente = false;
  codigoTicketSeleccionado = '';
  ticketIdSeleccionado: number | null = null;

  private navigationSubscription: Subscription;

  constructor(public router: Router, private route: ActivatedRoute, private ticketsService: TicketsService, private authService: AuthService, public utils: UserUtilsService) {
    this.currentUser$ = this.authService.currentUser$;

    // Suscribirse a los eventos de navegación
    this.navigationSubscription = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      // Recargar tickets cuando se navega de vuelta a /tickets
      if (event.url === '/tickets') {
        this.loadMyTickets();
      }
    });
  }

  ngOnInit() {
    this.nombreUser = localStorage.getItem('nombreUser') || 'Usuario';
    const idRol = localStorage.getItem('idRol');
    this.idRol = idRol || '';

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
            codigo: item.idTicket ? this.formatTicketCode(item.idTicket, item.tipoComprobante || item.idTipoComprobante) : (item.numeroDocumentoRechazado || 'N/A'),
            asunto: item.asunto,
            descripcion: item.descripcion,
            numDocRechazado: item.numeroDocumentoRechazado || '',
            fechaCreacion: this.formatDate(item.fechaCreacion),
            estado: this.mapEstado(item.estado),
            tipoIcono: this.mapTipoIcono(item.tipoComprobante || item.idTipoComprobante),
            tipoComprobante: item.tipoComprobante || this.getTipoComprobanteName(item.idTipoComprobante) || '',
            idTicket: item.idTicket,
            prioridad: this.mapPrioridad(item.prioridad || item.idPrioridad),
            agente: item.agente || '',
            fechaUltimaActualizacion: this.formatDate(item.fechaUltimaActualizacion),
            fechaCierre: this.formatDate(item.fechaCierre),
            rutaArchivo: item.rutaArchivo || '',
            nombreArchivo: item.nombre_archivo || ''
          } as Ticket));

          this.ticketsOriginales = mapped;
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
        console.error('No se pudo cargar tickets desde backend.', err);
        this.loadErrorMessage = err?.error?.message || 'No se pudo cargar sus tickets. Intente nuevamente.';
        this.isLoadingTickets = false;
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
    // Convertir id numérico a nombre
    const tipoNombre = this.getTipoComprobanteName(tipo);
    const t = tipoNombre.toLowerCase();
    if (t.includes('factura')) return 'fa-file-invoice';
    if (t.includes('boleta') || t.includes('boleta de venta')) return 'fa-receipt';
    return 'fa-ticket';
  }

  /**
   * Convierte el id de tipo de comprobante a nombre
   */
  private getTipoComprobanteName(id: string): string {
    if (!id) return '';

    // Si ya viene el nombre desde el backend, devolverlo directamente
    const tiposValidos = ['Factura', 'Boleta', 'Boleta de Venta'];
    if (tiposValidos.includes(id)) {
      return id;
    }

    // Mapear por id numérico (compatibilidad con versión anterior)
    switch (id) {
      case '1':
        return 'Factura';
      case '2':
        return 'Boleta de Venta';
      default:
        return id;
    }
  }

  /**
   * Formatea idTicket a un código con prefijo según tipo de comprobante.
   * Ej: (1, 'boleta') -> BXA-001 ; (1, 'factura') -> FXA-001
   */
  private formatTicketCode(idTicket: number | string, tipoComprobante?: string): string {
    const n = typeof idTicket === 'number' ? idTicket : parseInt(idTicket as string, 10) || 0;
    // Convertir id numérico a nombre antes de verificar
    const tipoNombre = this.getTipoComprobanteName(tipoComprobante || '');
    const t = tipoNombre.toLowerCase();
    const prefix = t.includes('boleta') ? 'BXA' : 'FXA';
    return `${prefix}-${n.toString().padStart(3, '0')}`;
  }

  /**
   * Devuelve el color del icono según el tipo de comprobante.
   */
  getIconColor(tipoComprobante?: string): string {
    // Convertir id numérico a nombre antes de verificar
    const tipoNombre = this.getTipoComprobanteName(tipoComprobante || '');
    const t = tipoNombre.toLowerCase();
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

  private mapEstado(estado: string): string {
    if (!estado) return 'Por Hacer';

    // Si ya viene el nombre desde el backend, devolverlo directamente
    const estadosValidos = ['Nuevo', 'Asignado', 'En Espera de Cliente', 'En Proceso (Técnico)', 'Propuesta Enviada', 'Cerrado (Solucionado)'];
    if (estadosValidos.includes(estado)) {
      return estado;
    }

    // Mapear por id numérico (compatibilidad con versión anterior)
    switch (estado) {
      case '1':
        return 'Nuevo';
      case '2':
        return 'Asignado';
      case '3':
        return 'En Espera de Cliente';
      case '4':
        return 'En Proceso (Técnico)';
      case '5':
        return 'Propuesta Enviada';
      case '6':
        return 'Cerrado (Solucionado)';
      default:
        // Fallback para nombres de estado antiguos
        const n = estado.toLowerCase();
        if (n.includes('asign') || n.includes('en progreso')) return 'Asignado';
        if (n.includes('nuevo') || n.includes('por hacer')) return 'Nuevo';
        if (n.includes('final') || n.includes('cerrad')) return 'Cerrado (Solucionado)';
        return estado;
    }
  }

  /**
   * Convierte el id de prioridad a nombre
   */
  private mapPrioridad(prioridad: string): string {
    if (!prioridad) return '';

    // Si ya viene el nombre desde el backend, devolverlo directamente
    const prioridadesValidas = ['Baja', 'Media', 'Alta'];
    if (prioridadesValidas.includes(prioridad)) {
      return prioridad;
    }

    // Mapear por id numérico (compatibilidad con versión anterior)
    switch (prioridad) {
      case '1':
        return 'Baja';
      case '2':
        return 'Media';
      case '3':
        return 'Alta';
      default:
        return prioridad;
    }
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

  // ⬅️ MÉTODO MODIFICADO
  editarTicket(codigo: string) {
    const idRol = localStorage.getItem('idRol');

    if (idRol === '3') {
      // Si es Agente de Soporte, abrir modal
      // Buscar el ticket para obtener su idTicket
      const ticket = this.tickets.find(t => t.codigo === codigo);
      if (ticket && ticket.idTicket) {
        this.codigoTicketSeleccionado = codigo;
        this.ticketIdSeleccionado = ticket.idTicket;
        this.mostrarModalAgente = true;
      }
    } else {
      // Si es Cliente, abrir panel lateral
      this.router.navigate(['editar', codigo], { relativeTo: this.route });
    }
  }

  // ⬅️ NUEVO MÉTODO
  cerrarModalAgente() {
    this.mostrarModalAgente = false;
    this.codigoTicketSeleccionado = '';
    this.ticketIdSeleccionado = null;
  }

  eliminarTicket(codigo: string) {
    // Buscar el ticket por código para obtener su idTicket
    const ticket = this.tickets.find(t => t.codigo === codigo);
    if (!ticket || !ticket.idTicket) {
      this.showAlertModal('Error', 'No se encontró el ticket o no tiene un ID válido.');
      return;
    }

    // Confirmar eliminación con modal
    this.showConfirmModal(
      'Confirmar eliminación',
      `¿Estás seguro de que deseas eliminar el ticket ${codigo}?`,
      () => {
        // Callback de confirmación
        this.ejecutarEliminacion(ticket.idTicket!);
      }
    );
  }

  private ejecutarEliminacion(idTicket: number) {
    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;

    this.ticketsService.deleteTicket(idTicket, token || undefined).subscribe({
      next: (res) => {
        // Eliminar el ticket de las listas locales
        this.ticketsOriginales = this.ticketsOriginales.filter(t => t.idTicket !== idTicket);
        this.tickets = this.tickets.filter(t => t.idTicket !== idTicket);

        // Mostrar mensaje de éxito
        this.showAlertModal('Eliminación', res.message || 'Ticket eliminado exitosamente.');
      },
      error: (err) => {
        this.showAlertModal('Error', err?.error?.message || 'No se pudo eliminar el ticket. Intente nuevamente.');
      }
    });
  }

  // Métodos para controlar el modal
  showConfirmModal(title: string, message: string, onConfirm: () => void) {
    this.modalConfig = {
      title,
      message,
      type: 'confirm',
      onConfirm: () => {
        this.closeModal();
        onConfirm();
      },
      onCancel: () => {
        this.closeModal();
      }
    };
    this.showModal = true;
  }

  showAlertModal(title: string, message: string) {
    this.modalConfig = {
      title,
      message,
      type: 'alert',
      onConfirm: () => {
        this.closeModal();
      },
      onCancel: () => {
        this.closeModal();
      }
    };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
  }

  getEstadoClass(estado: string): string {
    switch (estado) {
      case 'Nuevo':
        return 'estado-nuevo';
      case 'Asignado':
        return 'estado-asignado';
      case 'En Espera de Cliente':
        return 'estado-espera-cliente';
      case 'En Proceso (Técnico)':
        return 'estado-en-proceso';
      case 'Propuesta Enviada':
        return 'estado-propuesta-enviada';
      case 'Cerrado (Solucionado)':
        return 'estado-cerrado';
      default:
        return '';
    }
  }

  ngOnDestroy() {
    // Limpiar la suscripción al destruir el componente
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }
}