import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { InicioComponent } from './inicio/inicio.component';
import { TicketsComponent } from './tickets/tickets.component';
import { CrearticketComponent } from './crearticket/crearticket.component';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  { path: 'login', component: LoginComponent },

  {
    path: 'inicio',
    component: InicioComponent,
    canActivate: [authGuard]
  },

  {
    path: 'tickets',
    component: TicketsComponent,
    canActivate: [authGuard]
  },

  {
    path: 'crearticket',
    component: CrearticketComponent,
    canActivate: [authGuard]
  }
];
