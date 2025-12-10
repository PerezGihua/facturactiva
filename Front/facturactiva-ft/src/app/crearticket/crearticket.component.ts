import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TicketsService } from '../../services/tickets.service';

@Component({
  selector: 'app-crearticket',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './crearticket.component.html',
  styleUrls: ['./crearticket.component.css']
})
export class CrearticketComponent implements OnInit {

  codigoTicket: string = '';
  isEditMode: boolean = false;

  // Variables para el formulario
  codigo: string = '';
  asunto: string = '';
  tipo: string = '';
  descripcion: string = '';

  // Variables para el archivo
  selectedFile: File | null = null;
  previewUrl: string | null = null;
  isDragging: boolean = false;

  // Variables de estado
  isSubmitting: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ticketsService: TicketsService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['codigo']) {
        this.isEditMode = true;
        this.codigoTicket = params['codigo'];
        this.cargarDatosTicket(this.codigoTicket);
      } else {
        this.isEditMode = false;
        this.codigoTicket = '';
        this.codigo = '';
      }
    });
  }

  cargarDatosTicket(codigo: string) {
    console.log('Cargando datos del ticket:', codigo);

    // TODO: Aquí cuando tengas backend, harás la petición real
    // Por ahora simulamos que cargamos los datos
    this.codigo = codigo;

    // Ejemplo de cómo cargarías los otros datos:
    // this.asunto = 'Factura no aceptada';
    // this.tipo = 'Factura';
    // this.descripcion = 'Tengo una factura rechazada...';
  }

  // Manejar cuando se arrastra un archivo sobre el área
  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  // Manejar cuando se sale del área de drag
  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  // Manejar cuando se suelta un archivo
  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.handleFile(files[0]);
    }
  }

  // Manejar cuando se selecciona desde el input
  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.handleFile(input.files[0]);
    }
  }

  // Procesar el archivo
  handleFile(file: File) {
    // Validar que sea PNG
    if (file.type !== 'image/png') {
      alert('Solo se permiten archivos PNG');
      return;
    }

    // Validar tamaño (opcional, máximo 5MB)
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      alert('El archivo es muy pesado. Máximo 5MB');
      return;
    }

    this.selectedFile = file;

    // Generar vista previa
    const reader = new FileReader();
    reader.onload = (e: any) => {
      this.previewUrl = e.target.result;
    };
    reader.readAsDataURL(file);
  }

  // Eliminar archivo seleccionado
  removeFile() {
    this.selectedFile = null;
    this.previewUrl = null;
  }

  // Abrir selector de archivos
  triggerFileInput() {
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    fileInput?.click();
  }

  // Enviar formulario
  onSubmit() {
    // Limpiar mensajes previos
    this.errorMessage = '';
    this.successMessage = '';

    // Validaciones
    if (!this.codigo.trim()) {
      this.errorMessage = 'El número de documento es requerido';
      return;
    }

    if (!this.asunto.trim()) {
      this.errorMessage = 'El asunto es requerido';
      return;
    }

    if (!this.tipo) {
      this.errorMessage = 'El tipo de comprobante es requerido';
      return;
    }

    if (!this.descripcion.trim()) {
      this.errorMessage = 'La descripción es requerida';
      return;
    }

    // Preparar FormData
    const formData = new FormData();
    formData.append('documento', this.codigo);
    formData.append('asunto', this.asunto);
    formData.append('tipo', this.tipo);
    formData.append('descripcion', this.descripcion);

    // Agregar archivo si existe
    if (this.selectedFile) {
      formData.append('archivo', this.selectedFile);
    }

    // Obtener token
    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;

    // Enviar al backend
    this.isSubmitting = true;

    this.ticketsService.createTicket(formData, token || undefined).subscribe({
      next: (response) => {
        console.log('Ticket creado exitosamente:', response);
        this.isSubmitting = false;

        // Limpiar formulario
        this.resetForm();

        // Redirigir inmediatamente a la lista de tickets para que se recargue
        this.router.navigate(['/tickets']);
      },
      error: (error) => {
        console.error('Error al crear ticket:', error);
        this.errorMessage = error?.error?.message || 'Error al crear el ticket. Por favor, intente nuevamente.';
        this.isSubmitting = false;
      }
    });
  }

  // Resetear formulario
  resetForm() {
    this.codigo = '';
    this.asunto = '';
    this.tipo = '';
    this.descripcion = '';
    this.selectedFile = null;
    this.previewUrl = null;
  }
}
