import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService, PromptHistoryEntry } from '../../services/api.service';

interface UserProfile {
  id?: number;
  username?: string;
  email?: string;
  fullName?: string;
  role?: string;
  enabled?: boolean;
  createdAt?: string;
}

@Component({
  selector: 'app-perfilcomponent',
  standalone: false,
  templateUrl: './perfilcomponent.html',
  styleUrl: './perfilcomponent.css',
})
export class Perfilcomponent implements OnInit {
  profile: UserProfile = {
    username: localStorage.getItem('conclave_username') ?? 'Usuario',
    role: localStorage.getItem('conclave_role') ?? 'USER',
  };
  history: PromptHistoryEntry[] = [];
  profileError = '';
  historyError = '';

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.cargarPerfil();
    this.cargarHistorial();
  }

  cargarPerfil(): void {
    this.api.profile().subscribe({
      next: (data: unknown) => {
        if (data && typeof data === 'object') {
          this.profile = { ...this.profile, ...data as UserProfile };
        }
      },
      error: (error: HttpErrorResponse) => {
        this.profileError = this.obtenerMensajeError(error);
      },
    });
  }

  cargarHistorial(): void {
    this.historyError = '';
    this.api.promptHistory().subscribe({
      next: (data: PromptHistoryEntry[]) => {
        this.history = Array.isArray(data) ? data : [];
      },
      error: (error: HttpErrorResponse) => {
        this.history = [];
        this.historyError = this.obtenerMensajeError(error);
      },
    });
  }

  private obtenerMensajeError(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'No se pudo conectar con el servidor. Verifica que el backend esté iniciado.';
    }
    if (error.status === 401 || error.status === 403) {
      return 'Tu sesión ha expirado. Inicia sesión nuevamente.';
    }
    return 'No fue posible cargar la información. Inténtalo nuevamente.';
  }
}
