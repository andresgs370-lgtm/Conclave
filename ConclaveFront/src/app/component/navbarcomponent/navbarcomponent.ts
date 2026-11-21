import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Authservice } from '../../services/authservice';

/** Componente de navegación principal. */
@Component({
  selector: 'app-navbarcomponent',
  standalone: false,
  styleUrl: './navbarcomponent.css',
  templateUrl: './navbarcomponent.html',
})
export class Navbarcomponent {
  constructor(public auth: Authservice, private router: Router) {}

  /** Cierra la sesión y redirige al inicio. */
  logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
