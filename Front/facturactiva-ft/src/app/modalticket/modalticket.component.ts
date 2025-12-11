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
  numDocumento: string;
  descripcion: string;
  fechaCreacion: string;
  estado: string;
  documentoAdjunto?: string;
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
  @Output() cerrarModal = new EventEmitter<void>();

  ticket: TicketDetalle | null = null;
  comentarios: Comentario[] = [];
  nuevoComentario: string = '';
  respuestaTexto: { [key: number]: string } = {};
  mostrarRespuestas: { [key: number]: boolean } = {};

  estadosDisponibles = ['Por Hacer', 'En Progreso', 'Finalizado'];

  ngOnInit() {
    this.cargarTicket();
    this.cargarComentarios();
  }

  cargarTicket() {
    // TODO: Aquí irá la llamada al backend
    // Por ahora datos en duro
    this.ticket = {
      codigo: this.ticketCodigo,
      asunto: 'Rechazo de Boleta',
      numDocumento: '3215665465',
      descripcion: 'Tuve un problema con mi boleta, no me la aceptan.',
      fechaCreacion: '10/12/2025',
      estado: 'Por Hacer',
      documentoAdjunto: '/placeholder-document.png'
    };
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