import { Component } from '@angular/core';
import { Authservice } from '../../services/authservice';

/**
 * Página principal de Conclave: presentación, carrusel, preguntas frecuentes y accesos.
 * Los botones cambian según la sesión: sin sesión llevan a login y registro;
 * con sesión saludan al usuario y llevan directo a crear contenido o a su perfil.
 */
@Component({
  selector: 'app-paginaprincipalcomponent',
  standalone: false,
  templateUrl: './paginaprincipalcomponent.html',
  styleUrl: './paginaprincipalcomponent.css',
})
export class Paginaprincipalcomponent {

  /**
   * Inyecta el servicio de sesión para saber si el usuario ya inició sesión.
   *
   * @param auth servicio de sesión
   */
  constructor(public auth: Authservice) {}
}
