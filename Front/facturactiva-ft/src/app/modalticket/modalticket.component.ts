import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TicketsService } from '../../services/tickets.service';

interface Comentario {
  id: number;
  autor: string;
  rol: string; // 'agente' o 'cliente'
  tipoUsuario: string; // 'SOPORTE', 'CLIENTE', 'ADMIN', etc.
  texto: string;
  fecha: string;
  respuestas: Comentario[];
}

interface TicketDetalle {
  codigo: string;
  asunto: string;
  descripcion: string;
  numDocRechazado?: string;
  fechaCreacion: string;
  fechaUltimaActualizacion?: string;
  fechaCierre?: string;
  estado: string;
  prioridad?: string;
  tipoComprobante?: string;
  agente?: string;
  rutaArchivo?: string;
  nombreArchivo?: string;
  idTicket?: number;
}

@Component({
  selector: 'app-modalticket',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './modalticket.component.html',
  styleUrls: ['./modalticket.component.css']
})
export class ModalticketComponent implements OnInit {
  @Input() ticketCodigo: string = '';
  @Input() ticketId: number | null = null;
  @Input() ticketData: TicketDetalle | null = null;
  @Output() cerrarModal = new EventEmitter<void>();

  ticket: TicketDetalle | null = null;
  comentarios: Comentario[] = [];
  nuevoComentario: string = '';
  respuestaTexto: { [key: number]: string } = {};
  mostrarRespuestas: { [key: number]: boolean } = {};
  isLoadingTicket = false;

  estadosDisponibles = [
    'Nuevo',
    'Asignado',
    'En Espera de Cliente',
    'En Proceso (Técnico)',
    'Propuesta Enviada',
    'Cerrado (Solucionado)'
  ];

  constructor(private ticketsService: TicketsService) {}

  ngOnInit() {
    this.cargarTicket();
  }

  cargarTicket() {
    // Si se recibió ticketId, cargar desde el backend
    if (this.ticketId) {
      this.isLoadingTicket = true;
      const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;

      this.ticketsService.getTicketDetails(this.ticketId, token || undefined).subscribe({
        next: (response) => {
          this.mapearTicketDesdeBackend(response);
          this.isLoadingTicket = false;
        },
        error: (err) => {
          console.error('Error al cargar detalles del ticket:', err);
          this.isLoadingTicket = false;
          // Fallback a ticketData si existe
          if (this.ticketData) {
            this.ticket = { ...this.ticketData };
          }
        }
      });
    } else if (this.ticketData) {
      // Si se recibió ticketData del componente padre, usarlo
      this.ticket = { ...this.ticketData };
    } else {
      // Fallback: datos por defecto
      this.ticket = {
        codigo: this.ticketCodigo,
        asunto: 'Sin asunto',
        descripcion: 'Sin descripción',
        fechaCreacion: new Date().toLocaleDateString('es-PE'),
        estado: 'Nuevo'
      };
    }
  }

  mapearTicketDesdeBackend(response: any) {
    // Mapear datos básicos del ticket
    this.ticket = {
      codigo: this.formatTicketCode(response.idTicket, response.tipoComprobante?.nombre),
      asunto: response.asunto,
      descripcion: response.descripcion,
      numDocRechazado: response.numeroDocumentoRechazado,
      fechaCreacion: this.formatDate(response.fechaCreacion),
      fechaUltimaActualizacion: this.formatDate(response.fechaUltimaActualizacion),
      fechaCierre: this.formatDate(response.fechaCierre),
      estado: this.mapEstado(response.estado?.id || response.estado?.nombre || response.estado),
      prioridad: this.mapPrioridad(response.prioridad?.id || response.prioridad?.nombre || response.prioridad),
      tipoComprobante: response.tipoComprobante?.nombre || this.getTipoComprobanteName(response.tipoComprobante?.id),
      agente: response.agente || '',
      rutaArchivo: response.archivosAdjuntos?.[0]?.rutaArchivo || '',
      nombreArchivo: response.archivosAdjuntos?.[0]?.nombreArchivo || '',
      idTicket: response.idTicket
    };

    // Mapear comentarios
    if (response.comentarios && Array.isArray(response.comentarios)) {
      this.comentarios = this.mapearComentarios(response.comentarios);
    }
  }

  private mapEstado(estado: any): string {
    if (!estado) return 'Nuevo';

    // Si es un string y ya es un nombre válido, devolverlo
    if (typeof estado === 'string') {
      const estadosValidos = ['Nuevo', 'Asignado', 'En Espera de Cliente', 'En Proceso (Técnico)', 'Propuesta Enviada', 'Cerrado (Solucionado)'];
      if (estadosValidos.includes(estado)) {
        return estado;
      }
    }

    // Convertir id numérico a nombre
    const id = typeof estado === 'number' ? estado.toString() : estado;
    switch (id) {
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
        return 'Nuevo';
    }
  }

  private mapPrioridad(prioridad: any): string {
    if (!prioridad) return '';

    // Si es un string y ya es un nombre válido, devolverlo
    if (typeof prioridad === 'string') {
      const prioridadesValidas = ['Baja', 'Media', 'Alta'];
      if (prioridadesValidas.includes(prioridad)) {
        return prioridad;
      }
    }

    // Convertir id numérico a nombre
    const id = typeof prioridad === 'number' ? prioridad.toString() : prioridad;
    switch (id) {
      case '1':
        return 'Baja';
      case '2':
        return 'Media';
      case '3':
        return 'Alta';
      default:
        return '';
    }
  }

  private getTipoComprobanteName(id: any): string {
    if (!id) return '';

    // Si ya es un string válido, devolverlo
    if (typeof id === 'string') {
      const tiposValidos = ['Factura', 'Boleta', 'Boleta de Venta'];
      if (tiposValidos.includes(id)) {
        return id;
      }
    }

    // Convertir id numérico a nombre
    const idStr = typeof id === 'number' ? id.toString() : id;
    switch (idStr) {
      case '1':
        return 'Factura';
      case '2':
        return 'Boleta de Venta';
      default:
        return '';
    }
  }

  mapearComentarios(comentariosBackend: any[]): Comentario[] {
    return comentariosBackend.map(comentario => ({
      id: comentario.idComentario,
      // Mostrar nombre completo del usuario
      autor: comentario.nombreUsuario || comentario.nombreCompleto || 'Usuario',
      // Determinar rol basado en tipoUsuario del backend
      rol: this.determinarRol(comentario.tipoUsuario, comentario.esAgente),
      // Guardar el tipoUsuario original del backend
      tipoUsuario: comentario.tipoUsuario || 'CLIENTE',
      texto: comentario.contenido,
      fecha: this.formatDateTime(comentario.fechaCreacion),
      respuestas: comentario.respuestas && comentario.respuestas.length > 0
        ? this.mapearComentarios(comentario.respuestas)
        : []
    }));
  }

  // NUEVO MÉTODO: Determinar el rol correctamente
  private determinarRol(tipoUsuario?: string, esAgente?: boolean): string {
    // Primero verificar tipoUsuario si existe (más confiable)
    if (tipoUsuario) {
      const tipo = tipoUsuario.toUpperCase();
      // Si es SOPORTE, ADMIN, o AGENTE, es un agente
      if (tipo === 'SOPORTE' || tipo === 'ADMIN' || tipo === 'AGENTE') {
        return 'agente';
      }
      // Si es CLIENTE, es un cliente
      if (tipo === 'CLIENTE') {
        return 'cliente';
      }
    }
    
    // Fallback: usar esAgente si está disponible
    if (esAgente !== undefined && esAgente !== null) {
      return esAgente ? 'agente' : 'cliente';
    }
    
    // Por defecto, asumir cliente
    return 'cliente';
  }

  formatTicketCode(idTicket: number, tipoComprobante?: string): string {
    const t = tipoComprobante?.toLowerCase() || '';
    const prefix = t.includes('boleta') ? 'BXA' : 'FXA';
    return `${prefix}-${idTicket.toString().padStart(3, '0')}`;
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return '';
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const yyyy = d.getFullYear();
    return `${dd}/${mm}/${yyyy}`;
  }

  formatDateTime(dateStr?: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return '';
    return d.toLocaleString('es-PE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  cambiarEstado(nuevoEstado: string) {
    if (this.ticket) {
      this.ticket.estado = nuevoEstado;
      console.log('Estado cambiado a:', nuevoEstado);
      // TODO: Aquí irá la llamada al backend para actualizar el estado
    }
  }

  agregarComentario() {
    if (!this.nuevoComentario.trim() || !this.ticket?.idTicket) return;

    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;

    this.ticketsService.addComment(
      this.ticket.idTicket,
      this.nuevoComentario,
      null,
      token || undefined
    ).subscribe({
      next: (response) => {
        console.log('Comentario agregado exitosamente:', response);

        // Recargar los detalles del ticket para obtener el comentario actualizado
        this.cargarTicket();

        // Limpiar el textarea
        this.nuevoComentario = '';
      },
      error: (err) => {
        console.error('Error al agregar comentario:', err);
        alert(err?.error?.message || 'No se pudo agregar el comentario. Intente nuevamente.');
      }
    });
  }

  agregarRespuesta(comentarioId: number) {
    const textoRespuesta = this.respuestaTexto[comentarioId];
    if (!textoRespuesta || !textoRespuesta.trim() || !this.ticket?.idTicket) return;

    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;

    this.ticketsService.addComment(
      this.ticket.idTicket,
      textoRespuesta,
      comentarioId,
      token || undefined
    ).subscribe({
      next: (response) => {
        console.log('Respuesta agregada exitosamente:', response);

        // Recargar los detalles del ticket para obtener la respuesta actualizada
        this.cargarTicket();

        // Limpiar el textarea y cerrar el área de respuesta
        this.respuestaTexto[comentarioId] = '';
        this.mostrarRespuestas[comentarioId] = false;
      },
      error: (err) => {
        console.error('Error al agregar respuesta:', err);
        alert(err?.error?.message || 'No se pudo agregar la respuesta. Intente nuevamente.');
      }
    });
  }

  toggleRespuestas(comentarioId: number) {
    this.mostrarRespuestas[comentarioId] = !this.mostrarRespuestas[comentarioId];
  }

  cerrar() {
    this.cerrarModal.emit();
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
      // Fallback para estados antiguos
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