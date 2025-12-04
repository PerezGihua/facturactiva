import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TicketsComponent } from './tickets.component';

describe('TicketsComponent', () => {
  let component: TicketsComponent;
  let fixture: ComponentFixture<TicketsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketsComponent], // Standalone
    }).compileComponents();

    fixture = TestBed.createComponent(TicketsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });

  it('debería renderizar el template correctamente', () => {
    const compiled = fixture.nativeElement;
    expect(compiled).toBeTruthy();
  });

  it('debería tener un template que cargue sin errores', () => {
    expect(() => fixture.detectChanges()).not.toThrow();
  });
});
