import { Component } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
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
   * Nombre del usuario con sesión iniciada, o {@code null} si no hay sesión.
   * Es un signal para que la vista se actualice sola al iniciar o cerrar sesión.
   */
  readonly usuario;

  /**
   * Inyecta el servicio de sesión y convierte el nombre del usuario en un signal.
   *
   * @param auth servicio de sesión
   */
  constructor(auth: Authservice) {
    this.usuario = toSignal(auth.username$, { initialValue: null });
  }
}
