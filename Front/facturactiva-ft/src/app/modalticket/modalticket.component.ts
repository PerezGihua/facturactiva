import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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
  @Output() cerrarModal = new EventEmitter<void>();

  ticket: TicketDetalle | null = null;
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

  @Input() userRole: string = '';

    // Agrega este método antes del método cerrar():
    eliminarTicket() {
      if (!this.ticket || !this.ticket.idTicket) {
        alert('No se puede eliminar este ticket');
        return;
      }

      const confirmacion = confirm(`¿Estás seguro de que deseas eliminar el ticket ${this.ticket.codigo}?`);
      
      if (confirmacion) {
        // Aquí irá la llamada al backend para eliminar
        console.log('Eliminando ticket:', this.ticket.idTicket);
        
        // Por ahora solo cerramos el modal
        // TODO: Implementar llamada al servicio de tickets
        this.cerrar();
      }
    }


  ngOnInit() {
    this.cargarTicket();
    this.cargarComentarios();
  }

    cargarTicket() {
      // Si se recibió ticketData del componente padre, usarlo
      if (this.ticketData) {
        this.ticket = { ...this.ticketData };
      } else {
        // Datos de prueba
        this.ticket = {
          codigo: this.ticketCodigo || 'BXA-004',
          asunto: 'Rechazo de Boleta',
          numDocRechazado: '3215665465',
          descripcion: 'Tuve un problema con mi boleta, no me la aceptan.',
          fechaCreacion: '10/12/2025',
          estado: 'Nuevo'
        };
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
      autor: 'Rodrigo Gallardo', // El agente actual
      rol: 'agente',
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
      autor: 'Rodrigo Gallardo', // El agente actual
      rol: 'agente',
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