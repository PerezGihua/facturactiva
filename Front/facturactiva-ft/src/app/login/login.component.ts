import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Router } from '@angular/router';

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

  private apiUrl = 'http://localhost:8080/api/auth/login';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.loginForm = this.fb.group({
      usuario: ['', [Validators.required, Validators.email]],
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

  onSubmit() {

    this.submitted = true;
    this.errorMessage = '';
    
    if (this.loginForm.invalid) return;

    const loginData = {
      username: this.loginForm.value.usuario,
      password: this.loginForm.value.password
    };

    this.http.post<any>(this.apiUrl, loginData).subscribe({

      next: (response) => {

        console.log('Respuesta login:', response);

        if (response.message === "Autenticación exitosa") {

          localStorage.setItem('idRol', response.idRol);
          localStorage.setItem('nombreUser', response.nombreUser);
          this.router.navigate(['/inicio'], { replaceUrl: true });

        } else {

          this.errorMessage = 'Usuario o contraseña incorrectos';

        }
      },

      error: (err) => {

        console.error('Error backend:', err);
        this.errorMessage = 'No se pudo conectar con el servidor';

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