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
  documento: string = '';
  asunto: string = '';
  tipo: string = '1';
  descripcion: string = '';

  // Variables para el archivo
  selectedFile: File | null = null;
  previewUrl: string | null = null;
  isDragging: boolean = false;

  // Estados de carga y error
  isSubmitting: boolean = false;
  errorMessage: string = '';

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
      }
    });
  }

  cargarDatosTicket(codigo: string) {
    console.log('Cargando datos del ticket:', codigo);
    // TODO: Implementar cuando exista endpoint de edición
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

  // Enviar el formulario
  enviarTicket() {
    this.errorMessage = '';

    // Verificar token antes de enviar
    const token = localStorage.getItem('token');
    console.log('Token actual:', token ? 'Existe' : 'No existe');
    console.log('Token value:', token);

    // Validaciones
    if (!this.documento.trim()) {
      this.errorMessage = 'El número de documento es obligatorio';
      return;
    }

    if (!this.asunto.trim()) {
      this.errorMessage = 'El asunto es obligatorio';
      return;
    }

    if (!this.tipo) {
      this.errorMessage = 'El tipo de documento es obligatorio';
      return;
    }

    if (!this.descripcion.trim()) {
      this.errorMessage = 'La descripción es obligatoria';
      return;
    }

    // Crear FormData
    const formData = new FormData();
    formData.append('documento', this.documento);
    formData.append('asunto', this.asunto);
    formData.append('tipo', this.tipo);
    formData.append('descripcion', this.descripcion);

    // Agregar archivo si existe
    if (this.selectedFile) {
      formData.append('archivo', this.selectedFile);
    }

    console.log('Datos a enviar:', {
      documento: this.documento,
      asunto: this.asunto,
      tipo: this.tipo,
      descripcion: this.descripcion,
      archivo: this.selectedFile?.name
    });

    // Enviar al backend
    this.isSubmitting = true;
    this.ticketsService.createTicket(formData).subscribe({
      next: (response) => {
        console.log('Ticket creado exitosamente:', response);
        this.isSubmitting = false;
        // Redirigir a la lista de tickets
        this.router.navigate(['/tickets']);
      },
      error: (error) => {
        console.error('Error completo al crear ticket:', error);
        console.error('Status:', error.status);
        console.error('Error body:', error.error);
        this.isSubmitting = false;
        this.errorMessage = error.error?.message || error.error?.error || 'Error al crear el ticket. Intente nuevamente.';
      }
    });
  }
}