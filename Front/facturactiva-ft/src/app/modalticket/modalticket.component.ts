import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TicketsService } from '../../services/tickets.service';

interface Comentario {
  id: number;
  autor: string;
  rol: string; // 'agente' o 'cliente'
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
  @Input() ticketData: TicketDetalle | null = null;
  @Input() userRole: string = '';
  @Input() idRol: string = '';
  @Output() cerrarModal = new EventEmitter<void>();
  @Output() ticketActualizado = new EventEmitter<void>();

  ticket: TicketDetalle | null = null;
  
  // Campos editables (solo para cliente)
  asuntoEditable: string = '';
  descripcionEditable: string = '';
  numDocumentoEditable: string = '';
  
  // Estado de edición
  isEditMode: boolean = false;
  isSaving: boolean = false;

  comentarios: Comentario[] = [];
  nuevoComentario: string = '';
  respuestaTexto: { [key: number]: string } = {};
  mostrarRespuestas: { [key: number]: boolean } = {};

  estadosDisponibles = [
    'Nuevo',
    'Asignado',
    'En Espera de Cliente',
    'En Proceso (Técnico)',
    'Propuesta Enviada',
    'Cerrado (Solucionado)'
  ];

  // Modal de confirmación
  mostrarConfirmacion: boolean = false;
  tituloConfirmacion: string = '';
  mensajeConfirmacion: string = '';
  tipoConfirmacion: 'guardar' | 'eliminar' = 'guardar';
  accionPendiente: (() => void) | null = null;

  ngOnInit() {
    this.cargarTicket();
    this.cargarComentarios();
    
    // Determinar si es modo edición (solo para clientes)
    this.isEditMode = this.idRol === '1';
  }

  cargarTicket() {
    // Si se recibió ticketData del componente padre, usarlo
    if (this.ticketData) {
      this.ticket = { ...this.ticketData };
      
      // Inicializar campos editables
      this.asuntoEditable = this.ticket.asunto || '';
      this.descripcionEditable = this.ticket.descripcion || '';
      this.numDocumentoEditable = this.ticket.numDocRechazado || '';
    } else {
      // Fallback: datos por defecto si no se recibió ticketData
      this.ticket = {
        codigo: this.ticketCodigo,
        asunto: 'Sin asunto',
        descripcion: 'Sin descripción',
        fechaCreacion: new Date().toLocaleDateString('es-PE'),
        estado: 'Nuevo'
      };
      
      this.asuntoEditable = this.ticket.asunto;
      this.descripcionEditable = this.ticket.descripcion;
      this.numDocumentoEditable = '';
    }
  }

  cargarComentarios() {
    // TODO: Aquí irá la llamada al backend
    // Por ahora datos en duro
    this.comentarios = [
      {
        id: 1,
        autor: 'Rodrigo Gallardo',
        rol: 'agente',
        texto: '¿Podrías enviarme más detalles sobre el error que te aparece?',
        fecha: '10/12/2025 14:30',
        respuestas: [
          {
            id: 2,
            autor: 'Carlos Cliente',
            rol: 'cliente',
            texto: 'Claro, me dice "Documento rechazado por SUNAT"',
            fecha: '10/12/2025 15:00',
            respuestas: []
          }
        ]
      }
    ];
  }

  // Método para guardar cambios (MODIFICADO CON CONFIRMACIÓN)
  guardarCambios() {
    if (!this.ticket || !this.ticket.idTicket) {
      alert('No se puede guardar. Ticket inválido.');
      return;
    }

    // Validaciones
    if (!this.asuntoEditable.trim()) {
      alert('El asunto es requerido');
      return;
    }

    if (!this.descripcionEditable.trim()) {
      alert('La descripción es requerida');
      return;
    }

    if (!this.numDocumentoEditable.trim()) {
      alert('El número de documento es requerido');
      return;
    }

    // Mostrar modal de confirmación
    this.tituloConfirmacion = '¿Desea guardar cambios?';
    this.mensajeConfirmacion = 'Los cambios realizados se guardarán permanentemente.';
    this.tipoConfirmacion = 'guardar';
    this.accionPendiente = () => this.ejecutarGuardado();
    this.mostrarConfirmacion = true;
  }

  // Nuevo método para ejecutar el guardado
  private ejecutarGuardado() {
    // Preparar FormData
    const formData = new FormData();
    formData.append('documento', this.numDocumentoEditable);
    formData.append('asunto', this.asuntoEditable);
    formData.append('descripcion', this.descripcionEditable);
    formData.append('tipo', this.ticket?.tipoComprobante || '');

    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;

    this.isSaving = true;

    console.log('Guardando cambios del ticket:', this.ticket?.idTicket, formData);
    
    // Simular guardado exitoso por ahora
    setTimeout(() => {
      this.isSaving = false;
      
      // Actualizar los valores del ticket
      if (this.ticket) {
        this.ticket.asunto = this.asuntoEditable;
        this.ticket.descripcion = this.descripcionEditable;
        this.ticket.numDocRechazado = this.numDocumentoEditable;
      }
      
      alert('Cambios guardados exitosamente');
      
      // Emitir evento para que se recargue la lista
      this.ticketActualizado.emit();
    }, 1000);

    /* Implementación real cuando tengas el método en el servicio:
    this.ticketsService.updateTicket(this.ticket.idTicket, formData, token || undefined).subscribe({
      next: (response) => {
        console.log('Ticket actualizado:', response);
        this.isSaving = false;
        
        // Actualizar los valores del ticket
        if (this.ticket) {
          this.ticket.asunto = this.asuntoEditable;
          this.ticket.descripcion = this.descripcionEditable;
          this.ticket.numDocRechazado = this.numDocumentoEditable;
        }
        
        alert('Cambios guardados exitosamente');
        
        // Emitir evento para que se recargue la lista
        this.ticketActualizado.emit();
      },
      error: (error) => {
        console.error('Error al actualizar ticket:', error);
        this.isSaving = false;
        alert('Error al guardar los cambios. Intente nuevamente.');
      }
    });
    */
  }

  cambiarEstado(nuevoEstado: string) {
    if (this.ticket) {
      this.ticket.estado = nuevoEstado;
      console.log('Estado cambiado a:', nuevoEstado);
      // TODO: Aquí irá la llamada al backend para actualizar el estado
    }
  }

  agregarComentario() {
    if (!this.nuevoComentario.trim()) return;

    const nuevoComentarioObj: Comentario = {
      id: Date.now(),
      autor: 'Rodrigo Gallardo', // El usuario actual
      rol: this.idRol === '1' ? 'cliente' : 'agente',
      texto: this.nuevoComentario,
      fecha: new Date().toLocaleString('es-PE'),
      respuestas: []
    };

    this.comentarios.push(nuevoComentarioObj);
    this.nuevoComentario = '';
    
    // TODO: Aquí irá la llamada al backend para guardar el comentario
    console.log('Comentario agregado:', nuevoComentarioObj);
  }

  agregarRespuesta(comentarioId: number) {
    const textoRespuesta = this.respuestaTexto[comentarioId];
    if (!textoRespuesta || !textoRespuesta.trim()) return;

    const nuevaRespuesta: Comentario = {
      id: Date.now(),
      autor: 'Rodrigo Gallardo', // El usuario actual
      rol: this.idRol === '1' ? 'cliente' : 'agente',
      texto: textoRespuesta,
      fecha: new Date().toLocaleString('es-PE'),
      respuestas: []
    };

    // Buscar el comentario y agregar la respuesta
    this.agregarRespuestaRecursiva(this.comentarios, comentarioId, nuevaRespuesta);
    this.respuestaTexto[comentarioId] = '';
    
    // TODO: Aquí irá la llamada al backend
    console.log('Respuesta agregada:', nuevaRespuesta);
  }

  private agregarRespuestaRecursiva(comentarios: Comentario[], id: number, respuesta: Comentario): boolean {
    for (const comentario of comentarios) {
      if (comentario.id === id) {
        comentario.respuestas.push(respuesta);
        return true;
      }
      if (comentario.respuestas.length > 0) {
        if (this.agregarRespuestaRecursiva(comentario.respuestas, id, respuesta)) {
          return true;
        }
      }
    }
    return false;
  }

  toggleRespuestas(comentarioId: number) {
    this.mostrarRespuestas[comentarioId] = !this.mostrarRespuestas[comentarioId];
  }

  // Método para eliminar ticket (MODIFICADO CON CONFIRMACIÓN)
  eliminarTicket() {
    if (!this.ticket || !this.ticket.idTicket) {
      alert('No se puede eliminar este ticket');
      return;
    }

    // Mostrar modal de confirmación
    this.tituloConfirmacion = '¿Está seguro que quiere eliminar?';
    this.mensajeConfirmacion = `El ticket ${this.ticket.codigo} se eliminará permanentemente y no podrá recuperarse.`;
    this.tipoConfirmacion = 'eliminar';
    this.accionPendiente = () => this.ejecutarEliminacion();
    this.mostrarConfirmacion = true;
  }

  // Nuevo método para ejecutar la eliminación
  private ejecutarEliminacion() {
    // TODO: Implementar llamada al servicio de tickets
    console.log('Eliminando ticket:', this.ticket?.idTicket);
    
    // Emitir evento para recargar lista y cerrar modal
    this.ticketActualizado.emit();
    this.cerrar();
  }

  // Métodos para controlar el modal de confirmación
  confirmarAccion() {
    if (this.accionPendiente) {
      this.accionPendiente();
    }
    this.cerrarConfirmacion();
  }

  cancelarAccion() {
    this.cerrarConfirmacion();
  }

  private cerrarConfirmacion() {
    this.mostrarConfirmacion = false;
    this.accionPendiente = null;
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
  
  // Determinar si el usuario puede editar campos
  get puedeEditarCampos(): boolean {
    return this.idRol === '1'; // Solo clientes
  }
  
  // Determinar si el usuario puede cambiar el estado
  get puedeEditarEstado(): boolean {
    return this.idRol === '3'; // Solo agentes
  }
}