import { Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService, GenerationResult } from '../../services/api.service';

/** Componente para solicitar y visualizar videos generados. */
@Component({
  selector: 'app-videocomponent',
  standalone: false,
  styleUrl: './videocomponent.css',
  templateUrl: './videocomponent.html',
})
export class Videocomponent {
  prompt = '';
  results: GenerationResult[] = [];
  error = '';
  loading = false;
  format = 'MP4';

  /** Modelos de texto a video de Hugging Face disponibles. Cada uno usa su propia API key. */
  readonly videoModels: { value: string; label: string }[] = [
    { value: 'HF_MINIMAX_H3', label: 'MiniMax H3 FlashGen (rápido, con audio)' },
    { value: 'HF_MINIMAX_TURBO', label: 'MiniMax H3 Turbo (con audio)' },
    { value: 'HF_OMNI_VIDEO', label: 'Omni Video (3 segundos)' },
    { value: 'HF_MODELSCOPE', label: 'ModelScope Text-to-Video (clásico)' },
  ];

  /** Modelo de video seleccionado. */
  videoProvider = this.videoModels[0].value;

  private readonly genericError = 'No fue posible generar el contenido. Revisa la conexión con el backend.';
  loginRequired = !localStorage.getItem('conclave_token');

  constructor(private api: ApiService) {}

  /** Abre los enlaces de descarga de los resultados disponibles. */
  downloadAll(): void {
    this.results.filter(result => Boolean(result.downloadUrl)).forEach(result => window.open(result.downloadUrl, '_blank', 'noopener'));
  }

  /** Comparte el prompt o lo copia si compartir no está disponible. */
  sharePrompt(): void {
    const shareText = this.prompt;
    if (navigator.share) {
      navigator.share({ title: 'Prompt de Conclave', text: shareText }).catch((error: unknown) => this.handleShareError(error));
    } else {
      navigator.clipboard.writeText(shareText).catch(() => { this.error = 'No fue posible copiar el prompt.'; });
    }
  }

  /** Maneja errores al compartir, ignorando la cancelación del usuario. */
  private handleShareError(error: unknown): void {
    if (error instanceof Error && error.name !== 'AbortError') {
      this.error = 'No fue posible compartir el prompt.';
    }
  }

  /** Indica si el backend informó una configuración de proveedor ausente. */
  private static isConfigurationError(error: unknown): boolean {
    return error instanceof HttpErrorResponse && error.status === 503;
  }

  /** Convierte un error HTTP o de conexión en un mensaje legible. */
  private extractError(error: unknown): string {
    if (Videocomponent.isConfigurationError(error)) {
      return 'Falta configurar o activar la API key del modelo seleccionado. Un administrador debe configurarla en el panel de administración.';
    }
    const body = error instanceof HttpErrorResponse ? error.error : null;
    return Videocomponent.messageFromBody(body) ?? Videocomponent.messageFromHttpError(error) ?? this.genericError;
  }

  /** Obtiene un mensaje legible de la respuesta del servidor. */
  private static messageFromBody(body: unknown): string | null {
    const directMessage = Videocomponent.bodyText(body);
    if (directMessage !== null) return directMessage;
    const record = Videocomponent.bodyRecord(body);
    if (!record) return null;
    return Videocomponent.firstText(record, ['message', 'detail'])
      ?? Videocomponent.nestedErrorMessage(record['error']);
  }

  /** Devuelve el cuerpo cuando es texto no vacío. */
  private static bodyText(body: unknown): string | null {
    return typeof body === 'string' && body.trim() ? body : null;
  }

  /** Convierte el cuerpo a un registro cuando es un objeto. */
  private static bodyRecord(body: unknown): Record<string, unknown> | null {
    return body && typeof body === 'object' ? body as Record<string, unknown> : null;
  }

  /** Busca el primer texto disponible entre las propiedades indicadas. */
  private static firstText(record: Record<string, unknown>, keys: string[]): string | null {
    for (const key of keys) {
      const value = record[key];
      if (typeof value === 'string') return value;
    }
    return null;
  }

  /** Extrae el mensaje de una propiedad error anidada. */
  private static nestedErrorMessage(value: unknown): string | null {
    if (typeof value === 'string') return value;
    if (!value || typeof value !== 'object') return null;
    const nested = value as Record<string, unknown>;
    return typeof nested['message'] === 'string' ? nested['message'] : null;
  }

  /** Obtiene un mensaje HTTP cuando el cuerpo no aporta detalles. */
  private static messageFromHttpError(error: unknown): string | null {
    if (!(error instanceof HttpErrorResponse)) return null;
    if (error.status) return `Error HTTP ${error.status}: ${error.statusText || 'La solicitud fue rechazada por el servidor.'}`;
    return error.message || null;
  }

  /** Valida el prompt y solicita la generación del video. */
  generate(): void {
    this.error = '';
    this.results = [];
    if (!this.prompt.trim()) {
      this.error = 'Escribe un prompt para continuar.';
      return;
    }
    if (!localStorage.getItem('conclave_token')) {
      this.error = 'Inicia sesión para generar contenido.';
      return;
    }
    this.loading = true;
    this.api.generate(this.prompt, 'VIDEO', this.format, undefined, this.videoProvider).subscribe({
      next: (response) => {
        this.results = Array.isArray(response) ? response : [];
        this.loading = false;
        const failedResult = this.results.find(result => result.status !== 'SUCCESS');
        if (failedResult) this.error = failedResult.errorMessage || 'La generación no pudo completarse.';
      },
      error: (error: unknown) => {
        this.error = this.extractError(error);
        this.loading = false;
      },
    });
  }
}
