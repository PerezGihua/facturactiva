import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CrearticketComponent } from './crearticket.component';

describe('CrearticketComponent', () => {
  let component: CrearticketComponent;
  let fixture: ComponentFixture<CrearticketComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrearticketComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(CrearticketComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle panel', () => {
    const initial = component.isPanelOpen;
    component.togglePanel();
    expect(component.isPanelOpen).toBe(!initial);
  });
});
