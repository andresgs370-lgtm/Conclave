import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

/**
 * Página para verificar la cuenta con el código de 6 dígitos enviado al correo
 * después del registro.
 */
@Component({
  selector: 'app-verificarcomponent',
  standalone: false,
  templateUrl: './verificarcomponent.html',
  styleUrl: './verificarcomponent.css',
})
export class Verificarcomponent implements OnInit {

  /** Correo con el que se registró el usuario. */
  email = '';

  /** Código de verificación escrito por el usuario. */
  codigo = '';

  /** Indica si hay una solicitud en curso. */
  loading = false;

  /** Mensaje de error visible. Vacío cuando no hay error. */
  error = '';

  /** Mensaje de éxito visible. Vacío cuando no hay mensaje. */
  success = '';

  /**
   * Inyecta el servicio de API, la ruta activa y el enrutador.
   *
   * @param api servicio de comunicación con el backend
   * @param route ruta activa, de donde se lee el correo
   * @param router enrutador para volver al inicio de sesión
   */
  constructor(private api: ApiService, private route: ActivatedRoute, private router: Router) {}

  /** Toma el correo enviado como parámetro de la URL, si existe. */
  ngOnInit(): void {
    this.email = this.route.snapshot.queryParamMap.get('email') ?? '';
  }

  /** Valida el formulario y envía el código al backend. */
  verificar(): void {
    this.error = '';
    this.success = '';
    if (!this.email.trim()) {
      this.error = 'Escribe el correo con el que te registraste.';
      return;
    }
    if (!/^\d{6}$/.test(this.codigo.trim())) {
      this.error = 'El código debe tener 6 dígitos.';
      return;
    }
    this.loading = true;
    this.api.verificarRegistro(this.email.trim(), this.codigo.trim()).subscribe({
      next: mensaje => {
        this.loading = false;
        this.success = mensaje || 'Cuenta verificada. Ya puedes iniciar sesión.';
        setTimeout(() => this.router.navigate(['/login'], { queryParams: { verified: 'true' } }), 2000);
      },
      error: (e: HttpErrorResponse) => {
        this.loading = false;
        this.error = Verificarcomponent.mensajeError(e);
      },
    });
  }

  /** Solicita al backend un código nuevo de verificación para el correo escrito. */
  reenviar(): void {
    this.error = '';
    this.success = '';
    if (!this.email.trim()) {
      this.error = 'Escribe el correo con el que te registraste.';
      return;
    }
    this.loading = true;
    this.api.reenviarCodigo(this.email.trim()).subscribe({
      next: mensaje => {
        this.loading = false;
        this.codigo = '';
        this.success = mensaje || 'Código reenviado. Revisa tu correo.';
      },
      error: (e: HttpErrorResponse) => {
        this.loading = false;
        this.error = Verificarcomponent.mensajeError(e);
      },
    });
  }

  /**
   * Convierte un error HTTP en un mensaje legible para el usuario.
   *
   * @param e error devuelto por la petición
   * @returns mensaje de error
   */
  private static mensajeError(e: HttpErrorResponse): string {
    if (e.status === 0) return 'No fue posible conectar con el servidor.';
    if (typeof e.error === 'string' && e.error.trim()) return e.error;
    if (e.status === 400) return 'Código incorrecto.';
    if (e.status === 404) return 'No existe una cuenta con ese correo.';
    if (e.status === 410) return 'El código ha expirado. Pide uno nuevo con "Reenviar código".';
    return 'No fue posible verificar la cuenta.';
  }
}
