import { Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

/**
 * Página para recuperar la contraseña en dos pasos: primero se solicita un código
 * al correo y después se confirma la nueva contraseña con ese código.
 */
@Component({
  selector: 'app-recuperarcomponent',
  standalone: false,
  templateUrl: './recuperarcomponent.html',
  styleUrl: './recuperarcomponent.css',
})
export class Recuperarcomponent {

  /** Paso visible: solicitar el código o confirmar la nueva contraseña. */
  paso: 'email' | 'codigo' = 'email';

  /** Correo de la cuenta. */
  email = '';

  /** Código recibido por correo. */
  codigo = '';

  /** Nueva contraseña. */
  nuevaPassword = '';

  /** Confirmación de la nueva contraseña. */
  confirmarPassword = '';

  /** Indica si hay una solicitud en curso. */
  loading = false;

  /** Mensaje de error visible. Vacío cuando no hay error. */
  error = '';

  /** Mensaje de éxito visible. Vacío cuando no hay mensaje. */
  success = '';

  /**
   * Inyecta el servicio de API y el enrutador.
   *
   * @param api servicio de comunicación con el backend
   * @param router enrutador para volver al inicio de sesión
   */
  constructor(private api: ApiService, private router: Router) {}

  /** Solicita al backend el envío del código de restablecimiento al correo. */
  solicitarCodigo(): void {
    this.error = '';
    this.success = '';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email.trim())) {
      this.error = 'Escribe un correo válido.';
      return;
    }
    this.loading = true;
    this.api.solicitarReset(this.email.trim()).subscribe({
      next: mensaje => {
        this.loading = false;
        this.success = mensaje || 'Código enviado a tu correo.';
        this.paso = 'codigo';
      },
      error: (e: HttpErrorResponse) => {
        this.loading = false;
        this.error = Recuperarcomponent.mensajeError(e);
      },
    });
  }

  /** Valida el formulario y confirma la nueva contraseña con el código recibido. */
  confirmar(): void {
    this.error = '';
    this.success = '';
    if (!/^\d{6}$/.test(this.codigo.trim())) {
      this.error = 'El código debe tener 6 dígitos.';
      return;
    }
    if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/.test(this.nuevaPassword)) {
      this.error = 'La contraseña debe tener mínimo 8 caracteres, una minúscula, una mayúscula, un número y un símbolo.';
      return;
    }
    if (this.nuevaPassword !== this.confirmarPassword) {
      this.error = 'Las contraseñas no coinciden.';
      return;
    }
    this.loading = true;
    this.api.confirmarReset(this.email.trim(), this.codigo.trim(), this.nuevaPassword).subscribe({
      next: mensaje => {
        this.loading = false;
        this.success = mensaje || 'Contraseña actualizada correctamente.';
        setTimeout(() => this.router.navigate(['/login'], { queryParams: { reset: 'true' } }), 2000);
      },
      error: (e: HttpErrorResponse) => {
        this.loading = false;
        this.error = Recuperarcomponent.mensajeError(e);
      },
    });
  }

  /** Vuelve al primer paso para cambiar el correo. */
  cambiarCorreo(): void {
    this.paso = 'email';
    this.codigo = '';
    this.nuevaPassword = '';
    this.confirmarPassword = '';
    this.error = '';
    this.success = '';
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
    if (e.status === 410) return 'El código ha expirado. Solicita uno nuevo.';
    return 'No fue posible completar la solicitud.';
  }
}
