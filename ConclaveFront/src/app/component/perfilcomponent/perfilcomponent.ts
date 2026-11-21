import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { formatDate } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Apiservice, PromptHistoryEntry, ProviderKey } from '../../services/apiservice';
import { Authservice } from '../../services/authservice';

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
 * Si el usuario es administrador, también permite configurar las API keys de las IA.
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

  /** API keys registradas. Solo se cargan para administradores. */
  providerKeys: ProviderKey[] = [];

  /** Proveedor cuya API key se está editando, o {@code null} si no hay ninguno. */
  keyEditando: string | null = null;

  /** Valores del formulario de edición de la API key. */
  keyForm = { newKey: '', active: true };

  /** Mensaje de error de la sección de API keys. */
  keyError = '';

  /** Mensaje de confirmación de la sección de API keys. */
  keyMensaje = '';

  /** Indica si se está guardando una API key. */
  guardandoKey = false;

  /**
   * Inyecta los servicios de API y de sesión, y el detector de cambios.
   *
   * @param api servicio de comunicación con el backend
   * @param auth servicio de sesión, para saber si el usuario es administrador
   * @param cdr detector de cambios, necesario porque la aplicación no usa zone.js
   */
  constructor(private readonly api: Apiservice, private readonly auth: Authservice,
              private readonly cdr: ChangeDetectorRef) {}

  /** Indica si el usuario autenticado es administrador. */
  get esAdmin(): boolean {
    return this.auth.isAdmin;
  }

  /** Inicializa la información del perfil, el historial y, si es admin, las API keys. */
  ngOnInit(): void {
    this.cargarPerfil();
    this.cargarHistorial();
    if (this.esAdmin) this.cargarKeys();
  }

  /** Consulta las API keys de los proveedores. */
  cargarKeys(): void {
    this.keyError = '';
    this.api.listarApiKeys().subscribe({
      next: (data: ProviderKey[]) => {
        this.providerKeys = Array.isArray(data) ? data : [];
        this.cdr.detectChanges();
      },
      error: (error: HttpErrorResponse) => {
        this.providerKeys = [];
        this.keyError = error.status === 403
          ? 'Solo los administradores pueden configurar las API keys.'
          : Perfilcomponent.obtenerMensajeError(error);
        this.cdr.detectChanges();
      },
    });
  }

  /**
   * Abre el formulario de edición de un proveedor.
   *
   * @param key proveedor seleccionado
   */
  editarKey(key: ProviderKey): void {
    this.keyEditando = key.provider;
    this.keyForm = { newKey: '', active: key.active };
    this.keyError = '';
    this.keyMensaje = '';
  }

  /** Cierra el formulario de edición sin guardar. */
  cancelarKey(): void {
    this.keyEditando = null;
  }

  /** Guarda la API key y el estado del proveedor en edición. */
  guardarKey(): void {
    if (!this.keyEditando || this.guardandoKey) return;
    const provider = this.keyEditando;
    this.guardandoKey = true;
    this.api.actualizarApiKey(provider, this.keyForm.newKey.trim() || null, this.keyForm.active).subscribe({
      next: () => {
        this.guardandoKey = false;
        this.keyEditando = null;
        this.keyMensaje = 'API key de ' + provider + ' actualizada.';
        this.cargarKeys();
      },
      error: () => {
        this.guardandoKey = false;
        this.keyError = 'No se pudo actualizar la API key.';
        this.cdr.detectChanges();
      },
    });
  }

  /** Obtiene los datos de la cuenta desde el backend. */
  cargarPerfil(): void {
    this.api.profile().subscribe({
      next: (data: unknown) => {
        if (data && typeof data === 'object') {
          this.profile = { ...this.profile, ...data as UserProfile };
        }
        this.cdr.detectChanges();
      },
      error: (error: HttpErrorResponse) => {
        this.profileError = Perfilcomponent.obtenerMensajeError(error);
        this.cdr.detectChanges();
      },
    });
  }

  /** Consulta el historial de prompts del usuario autenticado. */
  cargarHistorial(): void {
    this.historyError = '';
    this.api.promptHistory().subscribe({
      next: (data: PromptHistoryEntry[]) => {
        this.history = Array.isArray(data) ? data : [];
        this.cdr.detectChanges();
      },
      error: (error: HttpErrorResponse) => {
        this.history = [];
        this.historyError = Perfilcomponent.obtenerMensajeError(error);
        this.cdr.detectChanges();
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
