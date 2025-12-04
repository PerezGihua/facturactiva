import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { InicioComponent } from './inicio/inicio.component';
import { TicketsComponent } from './tickets/tickets.component';
import { CrearticketComponent } from './crearticket/crearticket.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'inicio', component: InicioComponent },
  { path: 'tickets', component: TicketsComponent },
  { path: 'crearticket', component: CrearticketComponent }
];
