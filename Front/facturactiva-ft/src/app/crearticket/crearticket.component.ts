import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'appcrearticket-crearticket',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './crearticket.component.html',
  styleUrls: ['./crearticket.component.css']
})
export class CrearticketComponent implements OnInit {
  
  codigoTicket: string = '';
  isEditMode: boolean = false;

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    // Detectar si viene el parámetro 'codigo' en la ruta
    this.route.params.subscribe(params => {
      if (params['codigo']) {
        this.isEditMode = true;
        this.codigoTicket = params['codigo'];
        // Aquí cargarías los datos del ticket desde tu backend
        this.cargarDatosTicket(this.codigoTicket);
      } else {
        this.isEditMode = false;
        this.codigoTicket = '';
      }
    });
  }

  cargarDatosTicket(codigo: string) {
    // TODO: Aquí llamarás a tu servicio para obtener los datos del ticket
    console.log('Cargando datos del ticket:', codigo);
    // Ejemplo:
    // this.ticketService.getTicket(codigo).subscribe(data => {
    //   // Llenar el formulario con los datos
    // });
  }
}