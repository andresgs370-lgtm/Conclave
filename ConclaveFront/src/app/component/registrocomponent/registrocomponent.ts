import { Component, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { finalize } from 'rxjs/operators';

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

  submit(): void {
    this.error = '';

    if (!this.username.trim() || !this.email.trim() || !this.password || !this.confirm) {
      this.error = 'Completa todos los campos.';
      return;
    }

    if (this.password !== this.confirm) {
      this.error = 'Las contraseñas no coinciden.';
      return;
    }

    if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}$/.test(this.password)) {
      this.error = 'La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.';
      return;
    }

    if (!this.terms) {
      this.error = 'Debes aceptar los términos y condiciones.';
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
          const raw = err?.error;
          const message = (typeof raw === 'string' ? raw : raw?.message || raw?.error || '').toString().trim().toLowerCase();

          if (message.includes('nombre de usuario') || message.includes('username') || message.includes('usuario ya existe')) {
            this.error = 'Este nombre de usuario ya está registrado.';
          } else if (message.includes('correo') || message.includes('email') || message.includes('e-mail')) {
            this.error = 'Este correo electrónico ya está registrado.';
          } else if (message.includes('contraseña') || message.includes('password')) {
            this.error = 'La contraseña no cumple los requisitos.';
          } else if (err?.status === 400) {
            this.error = raw?.toString?.() || 'No se pudo crear la cuenta. Verifica los datos ingresados.';
          } else {
            this.error = 'No fue posible conectar con el servidor. Inténtalo nuevamente.';
          }

          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }
}
