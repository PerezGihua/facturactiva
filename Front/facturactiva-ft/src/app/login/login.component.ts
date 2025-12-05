import { Component } from '@angular/core';
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
export class LoginComponent {

  loginForm: FormGroup;
  submitted = false;
  showModal = false;

  private apiUrl = 'http://localhost:8080/api/auth/login';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      usuario: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  onSubmit() {

    this.submitted = true;
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

          alert(response.mensError || 'Usuario o contraseña incorrectos');

        }
      },

      error: (err) => {

        console.error('Error backend:', err);
        alert('No se pudo conectar con el servidor');

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
