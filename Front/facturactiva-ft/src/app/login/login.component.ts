import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']  
})
export class LoginComponent implements OnInit, OnDestroy {

  loginForm: FormGroup;
  submitted = false;
  showModal = false;
  errorMessage = '';
  isLoading = false;
  showPassword = false;
  private errorMessageTimeout: any;

    togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      // Bloquear zoom con Ctrl + Scroll
      window.addEventListener('wheel', this.preventZoom, { passive: false });
      // Bloquear zoom con Ctrl + / Ctrl -
      window.addEventListener('keydown', this.preventZoomKeys, { passive: false });
    }
  }

  ngOnDestroy() {
    if (isPlatformBrowser(this.platformId)) {
      // Remover listeners al salir
      window.removeEventListener('wheel', this.preventZoom);
      window.removeEventListener('keydown', this.preventZoomKeys);
      // Limpiar timeout de error message si existe
      if (this.errorMessageTimeout) {
        clearTimeout(this.errorMessageTimeout);
      }
    }
  }

  // Prevenir zoom con Ctrl + Scroll
  preventZoom = (e: WheelEvent) => {
    if (e.ctrlKey) {
      e.preventDefault();
    }
  }

  // Prevenir zoom con Ctrl + / Ctrl -
  preventZoomKeys = (e: KeyboardEvent) => {
    if (e.ctrlKey && (e.key === '+' || e.key === '-' || e.key === '0')) {
      e.preventDefault();
    }
  }

  get f() {
    return this.loginForm.controls;
  }

  /**
   * Establece el mensaje de error y lo limpia después de 10 segundos
   */
  private setErrorMessage(message: string) {
    this.errorMessage = message;
    
    // Limpiar timeout anterior si existe
    if (this.errorMessageTimeout) {
      clearTimeout(this.errorMessageTimeout);
    }
    
    // Establecer nuevo timeout para limpiar el mensaje después de 10 segundos
    if (isPlatformBrowser(this.platformId)) {
      this.errorMessageTimeout = setTimeout(() => {
        this.errorMessage = '';
        this.errorMessageTimeout = null;
      }, 10000);
    }
  }

  onSubmit() {
    this.submitted = true;
    this.errorMessage = '';
    
    if (this.loginForm.invalid) return;

    this.isLoading = true;
    const { email, password } = this.loginForm.value;

    this.authService.login(email, password).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.message === 'Autenticación exitosa') {
          console.log('Login exitoso:', response);
          this.router.navigate(['/inicio'], { replaceUrl: true });
        } else {
          this.setErrorMessage('Usuario o contraseña incorrectos');
        }
      },
      error: (err: any) => {
        this.isLoading = false;
        console.error('Error login:', err);
        
        // Intentar obtener el mensaje del error
        // El error viene como HttpErrorResponse
        if (err.error?.message) {
          this.setErrorMessage(err.error.message);
        } else if (err.message) {
          this.setErrorMessage(err.message);
        } else if (err.status === 0) {
          this.setErrorMessage('No se pudo conectar con el servidor');
        } else {
          this.setErrorMessage('Usuario o contraseña incorrectos');
        }
      }
    });
  }

  openModal() {
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
  }

}