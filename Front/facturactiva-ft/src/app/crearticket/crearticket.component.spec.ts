import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CrearticketComponent } from './crearticket.component';

describe('CrearticketComponent', () => {
  let component: CrearticketComponent;
  let fixture: ComponentFixture<CrearticketComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearticketComponent], // standalone
    }).compileComponents();

    fixture = TestBed.createComponent(CrearticketComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });

  it('debería tener el panel cerrado inicialmente', () => {
    expect(component.isPanelOpen).toBeFalse();
  });

  it('debería abrir el panel cuando se llama togglePanel()', () => {
    component.togglePanel();
    expect(component.isPanelOpen).toBeTrue();
  });

  it('debería cerrar el panel si se llama togglePanel() dos veces', () => {
    component.togglePanel();
    component.togglePanel();
    expect(component.isPanelOpen).toBeFalse();
  });

  it('el template debería renderizar correctamente', () => {
    const compiled = fixture.nativeElement;
    expect(compiled).toBeTruthy();
  });
});
