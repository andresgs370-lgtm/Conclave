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
          this.error = Registrocomponent.registrationError(err);

          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  /** Valida los campos requeridos antes de registrar la cuenta. */
  private validateForm(): string {
    return this.requiredFieldsError()
      || this.passwordMatchError()
      || this.passwordStrengthError()
      || this.termsError();
  }

  /** Comprueba que los campos obligatorios estén completos. */
  private requiredFieldsError(): string {
    return !this.username.trim() || !this.email.trim() || !this.password || !this.confirm
      ? 'Completa todos los campos.' : '';
  }

  /** Comprueba que ambas contraseñas coincidan. */
  private passwordMatchError(): string {
    return this.password !== this.confirm ? 'Las contraseñas no coinciden.' : '';
  }

  /** Comprueba la longitud y composición mínima de la contraseña. */
  private passwordStrengthError(): string {
    const valid = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}$/.test(this.password);
    return valid ? '' : 'La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.';
  }

  /** Comprueba la aceptación de términos y condiciones. */
  private termsError(): string {
    return this.terms ? '' : 'Debes aceptar los términos y condiciones.';
  }

  /** Traduce los errores de registro a mensajes comprensibles. */
  private static registrationError(err: unknown): string {
    const raw = Registrocomponent.errorPayload(err);
    const message = Registrocomponent.errorText(raw).toLowerCase();
    return Registrocomponent.usernameError(message)
      || Registrocomponent.emailError(message)
      || Registrocomponent.passwordError(message)
      || Registrocomponent.badRequestError(err, raw)
      || 'No fue posible conectar con el servidor. Inténtalo nuevamente.';
  }

  /** Extrae el cuerpo de error de una respuesta HTTP. */
  private static errorPayload(err: unknown): unknown {
    if (!err || typeof err !== 'object' || !('error' in err)) return null;
    return (err as { error?: unknown }).error;
  }

  /** Traduce errores relacionados con el nombre de usuario. */
  private static usernameError(message: string): string {
    return ['nombre de usuario', 'username', 'usuario ya existe'].some(term => message.includes(term))
      ? 'Este nombre de usuario ya está registrado.' : '';
  }

  /** Traduce errores relacionados con el correo electrónico. */
  private static emailError(message: string): string {
    return ['correo', 'email', 'e-mail'].some(term => message.includes(term))
      ? 'Este correo electrónico ya está registrado.' : '';
  }

  /** Traduce errores relacionados con la contraseña. */
  private static passwordError(message: string): string {
    return message.includes('contraseña') || message.includes('password')
      ? 'La contraseña no cumple los requisitos.' : '';
  }

  /** Devuelve el mensaje del servidor para errores HTTP 400. */
  private static badRequestError(err: unknown, raw: unknown): string {
    if (!err || typeof err !== 'object' || !('status' in err)) return '';
    return (err as { status?: number }).status === 400
      ? Registrocomponent.errorText(raw) || 'No se pudo crear la cuenta. Verifica los datos ingresados.'
      : '';
  }

  /** Convierte el cuerpo de error del backend a texto. */
  private static errorText(raw: unknown): string {
    if (typeof raw === 'string') return raw;
    return Registrocomponent.errorObjectText(raw);
  }

  /** Obtiene message o error cuando el cuerpo es un objeto. */
  private static errorObjectText(raw: unknown): string {
    if (!raw || typeof raw !== 'object') return '';
    const body = raw as { message?: unknown; error?: unknown };
    return String(body.message || body.error || '');
  }
}
