import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';

describe('LoginComponent FULL', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let httpMock: HttpTestingController;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [
        LoginComponent,
        HttpClientTestingModule,
        ReactiveFormsModule
      ],
      providers: [
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;

    httpMock = TestBed.inject(HttpTestingController);

    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  // 1. Creación
  it('debe crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  // 2. Getter f()
  it('getter f debe retornar los controles del formulario', () => {
    expect(component.f).toBe(component.loginForm.controls);
  });

  // 3. FORM INVALIDO → NO llama backend
  it('no debe llamar al backend si el formulario es inválido', () => {
    spyOn(component['http'], 'post').and.callThrough();

    component.loginForm.get('usuario')?.setValue('');
    component.loginForm.get('password')?.setValue('');

    component.onSubmit();

    expect(component['http'].post).not.toHaveBeenCalled();
  });

  // 4. Submit válido: backend OK (message "0")
  it('debe enviar un login válido y navegar', () => {
    spyOn(window, 'alert');

    component.loginForm.get('usuario')?.setValue('test@example.com');
    component.loginForm.get('password')?.setValue('123456');

    component.onSubmit();

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    expect(req.request.method).toBe('POST');

    req.flush({
      message: '0',
      nombreUser: 'Juan',
      roleName: 'Admin'
    });

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/inicio']);
    expect(window.alert).not.toHaveBeenCalled();
  });

  // 5. Backend responde con error lógico (message != "0")
  it('debe manejar respuestas de error del backend (message distinto de 0)', () => {
    spyOn(window, 'alert');

    component.loginForm.get('usuario')?.setValue('test@example.com');
    component.loginForm.get('password')?.setValue('123456');

    component.onSubmit();

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');

    req.flush({
      message: '1',
      mensError: 'Credenciales inválidas'
    });

    expect(window.alert).toHaveBeenCalledWith('Credenciales inválidas');
  });

  // 6. Error del servidor (HttpError)
  it('debe manejar error del servidor', () => {
    spyOn(window, 'alert');

    component.loginForm.get('usuario')?.setValue('test@example.com');
    component.loginForm.get('password')?.setValue('123456');

    component.onSubmit();

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    req.error(new ProgressEvent('error'));

    expect(window.alert).toHaveBeenCalledWith('No se pudo conectar con el servidor');
  });

  // 7. Verifica submitted se vuelve true
  it('debe marcar submitted en true al enviar el formulario', () => {
    component.loginForm.get('usuario')?.setValue('test@example.com');
    component.loginForm.get('password')?.setValue('123456');

    expect(component.submitted).toBeFalse();
    component.onSubmit();
    expect(component.submitted).toBeTrue();
  });

  // 8. No debe navegar si message != "0"
  it('no debe navegar si backend no retorna 0', () => {
    spyOn(window, 'alert');

    component.loginForm.get('usuario')?.setValue('test@example.com');
    component.loginForm.get('password')?.setValue('123456');

    component.onSubmit();

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    req.flush({ message: '2', mensError: 'Error X' });

    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  // 9. Test async con fakeAsync/tick
  it('debe manejar correctamente el flujo async', fakeAsync(() => {
    component.loginForm.get('usuario')?.setValue('async@test.com');
    component.loginForm.get('password')?.setValue('123456');

    component.onSubmit();
    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');

    tick(300);

    req.flush({
      message: '0',
      nombreUser: 'AsyncUser',
      roleName: 'Tester'
    });

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/inicio']);
  }));

  // 10. Abrir modal
  it('debe abrir el modal', () => {
    component.openModal();
    expect(component.showModal).toBeTrue();
  });

  // 11. Cerrar modal
  it('debe cerrar el modal', () => {
    component.showModal = true;
    component.closeModal();
    expect(component.showModal).toBeFalse();
  });

});
