import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalticketComponent } from './modalticket.component';

describe('ModalticketComponent', () => {
  let component: ModalticketComponent;
  let fixture: ComponentFixture<ModalticketComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalticketComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModalticketComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
