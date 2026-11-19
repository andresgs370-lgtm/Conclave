import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

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

  constructor(private api: ApiService, private auth: AuthService, private router: Router, private route: ActivatedRoute) {
    this.route.queryParamMap.subscribe((params) => {
      if (params.get('registered') === 'true') {
        this.success = 'Cuenta creada exitosamente. Ahora puedes iniciar sesión.';
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
