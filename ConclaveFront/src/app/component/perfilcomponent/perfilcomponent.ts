import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

interface UserProfile {
  username?: string;
  email?: string;
  fullName?: string;
  role?: string;
  enabled?: boolean;
}

/** Componente que consulta y presenta el perfil del usuario autenticado. */
@Component({
  selector: 'app-perfilcomponent',
  standalone: false,
  templateUrl: './perfilcomponent.html',
  styleUrl: './perfilcomponent.css',
})
export class Perfilcomponent implements OnInit {
  profile: UserProfile | null = null;
  loading = true;
  error = '';

  constructor(private api: ApiService) {}

  /** Carga los datos del perfil al iniciar el componente. */
  ngOnInit(): void {
    this.api.profile().subscribe({
      next: data => { this.profile = data as UserProfile; this.loading = false; },
      error: err => { this.error = err.error?.message || 'No fue posible cargar el perfil.'; this.loading = false; }
    });
  }
}
