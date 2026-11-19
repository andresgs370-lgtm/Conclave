import { Component, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

/** Componente para registrar una nueva cuenta. */
@Component({
  selector: 'app-registrocomponent',
  standalone: false,
  styleUrl: './registrocomponent.css',
  templateUrl: './registrocomponent.html',
})
export class Registrocomponent {
  username = '';
  email = '';
  password = '';
  confirm = '';
  terms = false;
  error = '';
  loading = false;

  constructor(private api: ApiService, private router: Router, private cdr: ChangeDetectorRef) {}

  /** Valida los datos y envía la solicitud de registro. */
  submit(): void {
    this.error = '';

    const validationError = this.validateForm();
    if (validationError) {
      this.error = validationError;
      return;
    }

    this.loading = true;
    this.cdr.detectChanges();

    this.api.register(this.username.trim(), this.email.trim(), this.password)
      .subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/login'], { queryParams: { registered: 'true' } });
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.error = this.registrationError(err);

          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  /** Valida los campos requeridos antes de registrar la cuenta. */
  private validateForm(): string {
    if (!this.username.trim() || !this.email.trim() || !this.password || !this.confirm) return 'Completa todos los campos.';
    if (this.password !== this.confirm) return 'Las contraseñas no coinciden.';
    if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}$/.test(this.password)) {
      return 'La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.';
    }
    if (!this.terms) return 'Debes aceptar los términos y condiciones.';
    return '';
  }

  /** Traduce los errores de registro a mensajes comprensibles. */
  private registrationError(err: unknown): string {
    const raw = err && typeof err === 'object' && 'error' in err ? (err as {error?: unknown}).error : null;
    const message = Registrocomponent.errorText(raw).toLowerCase();
    if (message.includes('nombre de usuario') || message.includes('username') || message.includes('usuario ya existe')) return 'Este nombre de usuario ya está registrado.';
    if (message.includes('correo') || message.includes('email') || message.includes('e-mail')) return 'Este correo electrónico ya está registrado.';
    if (message.includes('contraseña') || message.includes('password')) return 'La contraseña no cumple los requisitos.';
    if (err && typeof err === 'object' && 'status' in err && (err as {status?: number}).status === 400) {
      return Registrocomponent.errorText(raw) || 'No se pudo crear la cuenta. Verifica los datos ingresados.';
    }
    return 'No fue posible conectar con el servidor. Inténtalo nuevamente.';
  }

  /** Convierte el cuerpo de error del backend a texto. */
  private static errorText(raw: unknown): string {
    if (typeof raw === 'string') return raw;
    if (raw && typeof raw === 'object') {
      const body = raw as {message?: unknown; error?: unknown};
      return String(body.message || body.error || '');
    }
    return '';
  }
}
