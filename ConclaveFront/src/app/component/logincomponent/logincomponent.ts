import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Apiservice } from '../../services/apiservice';
import { Authservice } from '../../services/authservice';

/** Componente de inicio de sesión. */
@Component({
  selector: 'app-logincomponent',
  standalone: false,
  styleUrl: './logincomponent.css',
  templateUrl: './logincomponent.html',
})
export class Logincomponent {
  username = '';
  password = '';
  error = '';
  success = '';
  loading = false;

  /** Mensajes de éxito que se muestran según el parámetro recibido en la URL. */
  private static readonly mensajes: Record<string, string> = {
    registered: 'Cuenta creada exitosamente. Ahora puedes iniciar sesión.',
    verified: 'Cuenta verificada. Ya puedes iniciar sesión.',
    reset: 'Contraseña actualizada. Inicia sesión con tu nueva contraseña.',
  };

  constructor(private api: Apiservice, private auth: Authservice, private router: Router, private route: ActivatedRoute) {
    this.route.queryParamMap.subscribe((params) => {
      const clave = Object.keys(Logincomponent.mensajes).find(key => params.get(key) === 'true');
      if (clave) {
        this.success = Logincomponent.mensajes[clave];
      }
    });
  }

  /** Envía las credenciales y establece la sesión del usuario. */
  submit(): void {
    this.error = '';
    this.success = '';
    if (!this.username.trim() || !this.password) {
      this.error = 'Ingresa tu nombre de usuario y contraseña.';
      return;
    }

    this.loading = true;
    this.api.login(this.username.trim(), this.password).subscribe({
      next: (response) => {
        this.auth.setSession(response);
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.error = typeof err.error === 'string' ? err.error : 'No fue posible iniciar sesión.';
        this.loading = false;
      }
    });
  }
}
