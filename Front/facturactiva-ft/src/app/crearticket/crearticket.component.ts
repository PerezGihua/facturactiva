import { Component } from '@angular/core';

@Component({
  selector: 'appcrearticket-crearticket',
  standalone: true,
  templateUrl: './crearticket.component.html',
  styleUrls: ['./crearticket.component.css']
})
export class CrearticketComponent {
  isPanelOpen = false;

  togglePanel() {
    this.isPanelOpen = !this.isPanelOpen;
  }
}
