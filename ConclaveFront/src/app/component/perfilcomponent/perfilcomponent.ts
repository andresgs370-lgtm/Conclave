import { Component, OnInit } from '@angular/core';
import { formatDate } from '@angular/common';
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

/**
 * Gestiona la visualización del perfil del usuario y su historial de prompts.
 */
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
  private readonly textoVacio = '';
  private readonly locale = 'en-US';

  constructor(private readonly api: ApiService) {}

  /** Inicializa la información del perfil y el historial de prompts. */
  ngOnInit(): void {
    this.cargarPerfil();
    this.cargarHistorial();
  }

  /** Obtiene los datos de la cuenta desde el backend. */
  cargarPerfil(): void {
    this.api.profile().subscribe({
      next: (data: unknown) => {
        if (data && typeof data === 'object') {
          this.profile = { ...this.profile, ...data as UserProfile };
        }
      },
      error: (error: HttpErrorResponse) => {
        this.profileError = Perfilcomponent.obtenerMensajeError(error);
      },
    });
  }

  /** Consulta el historial de prompts del usuario autenticado. */
  cargarHistorial(): void {
    this.historyError = '';
    this.api.promptHistory().subscribe({
      next: (data: PromptHistoryEntry[]) => {
        this.history = Array.isArray(data) ? data : [];
      },
      error: (error: HttpErrorResponse) => {
        this.history = [];
        this.historyError = Perfilcomponent.obtenerMensajeError(error);
      },
    });
  }

  /** Devuelve un texto alternativo cuando el valor está vacío. */
  mostrarValor(valor: string | null | undefined, alternativa: string): string {
    return valor || alternativa || this.textoVacio;
  }

  /** Devuelve la fecha del registro con el formato utilizado en el historial. */
  mostrarFecha(fecha: string | null | undefined): string {
    return fecha ? formatDate(fecha, 'dd/MM/yyyy, HH:mm', this.locale) : 'Fecha no disponible';
  }

  /** Indica si la cuenta debe mostrarse como activa. */
  cuentaActiva(): string {
    return this.profile.enabled === false ? 'Desactivada' : 'Activa';
  }

  /** Indica si el historial está vacío y no existe un error de carga. */
  historialVacio(): boolean {
    return this.history.length === 0 && !this.historyError;
  }

  /** Indica si existen registros para mostrar en el historial. */
  historialConRegistros(): boolean {
    return this.history.length > 0;
  }

  /** Indica si un registro contiene un formato definido. */
  tieneFormato(item: PromptHistoryEntry): boolean {
    return Boolean(item.format ?? this.textoVacio);
  }

  /**
   * Convierte los errores HTTP en mensajes comprensibles para el usuario.
   *
   * @param error respuesta de error recibida desde el backend
   * @returns mensaje que se debe mostrar en la interfaz
   */
  private static obtenerMensajeError(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'No se pudo conectar con el servidor. Verifica que el backend esté iniciado.';
    }
    if (error.status === 401 || error.status === 403) {
      return 'Tu sesión ha expirado. Inicia sesión nuevamente.';
    }
    return 'No fue posible cargar la información. Inténtalo nuevamente.';
  }
}
