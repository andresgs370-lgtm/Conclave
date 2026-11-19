import { Component } from '@angular/core';
import { ApiService } from '../../services/api.service';

type GeneratedMediaType = 'image' | 'video' | 'audio' | 'other';

interface GenerationResult {
  model?: string;
  provider?: string;
  status?: string;
  textContent?: string;
  errorMessage?: string;
  downloadUrl?: string;
  mimeType?: string;
  durationMs?: number;
  mediaType: GeneratedMediaType;
  statusClass: string;
}

/** Componente para generar y visualizar imágenes. */
@Component({
  selector: 'app-imagencomponent',
  standalone: false,
  styleUrl: './imagencomponent.css',
  templateUrl: './imagencomponent.html',
})
export class Imagencomponent {
  prompt = '';
  results: GenerationResult[] = [];
  error = '';
  loading = false;
  format = 'PNG';
  imageProvider = 'CLOUDFLARE';
  loginRequired = !localStorage.getItem('conclave_token');

  constructor(private api: ApiService) {}

  /** Abre los enlaces de descarga de los resultados disponibles. */
  downloadAll(): void {
    this.results
      .filter((result) => Boolean(result.downloadUrl))
      .forEach((result) => window.open(result.downloadUrl, '_blank', 'noopener'));
  }

  /** Comparte el prompt o lo copia cuando Web Share no está disponible. */
  sharePrompt(): void {
    const shareText = this.prompt;
    if (navigator.share) {
      navigator.share({ title: 'Prompt de Conclave', text: shareText }).catch(() => {
        // Sharing can be cancelled by the user.
      });
    } else {
      navigator.clipboard.writeText(shareText).catch(() => { this.error = 'No fue posible copiar el prompt.'; });
    }
  }

  /** Valida la sesión y solicita la generación de imágenes. */
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
    this.api.generate(this.prompt, 'IMAGE', this.format, this.imageProvider).subscribe({
      next: (response: unknown) => {
        this.results = Imagencomponent.normalizeResults(response);
        this.loading = false;
      },
      error: (err: unknown) => {
        this.error = Imagencomponent.getErrorMessage(err);
        this.loading = false;
      },
    });
  }

  /** Normaliza la respuesta del backend para mostrar cada resultado. */
  private static normalizeResults(response: unknown): GenerationResult[] {
    if (!Array.isArray(response)) return [];
    return response.map((item: unknown) => {
      const result = item as Omit<GenerationResult, 'mediaType' | 'statusClass'>;
      const mimeType = result.mimeType || '';
      return {
        ...result,
        mediaType: Imagencomponent.getMediaType(mimeType),
        statusClass: result.status === 'SUCCESS' ? 'bg-success' : 'bg-danger',
      };
    });
  }

  /** Determina el tipo de contenido a partir del MIME recibido. */
  private static getMediaType(mimeType: string): GeneratedMediaType {
    if (mimeType.startsWith('image/')) return 'image';
    if (mimeType.startsWith('video/')) return 'video';
    if (mimeType.startsWith('audio/')) return 'audio';
    return 'other';
  }

  /** Convierte una respuesta de error en un mensaje para el usuario. */
  private static getErrorMessage(error: unknown): string {
    const directMessage = Imagencomponent.stringMessage(error);
    if (directMessage) return directMessage;
    const payload = Imagencomponent.errorPayload(error);
    return Imagencomponent.messageFromPayload(payload) || 'No fue posible generar el contenido.';
  }

  /** Obtiene el mensaje directo cuando el error es texto. */
  private static stringMessage(error: unknown): string | null {
    return typeof error === 'string' ? error : null;
  }

  /** Obtiene el cuerpo de error cuando existe la propiedad correspondiente. */
  private static errorPayload(error: unknown): unknown {
    if (!error || typeof error !== 'object' || !('error' in error)) return null;
    return (error as { error?: unknown }).error;
  }

  /** Extrae un mensaje de error de una respuesta del servidor. */
  private static messageFromPayload(payload: unknown): string | null {
    if (typeof payload === 'string') return payload;
    const message = Imagencomponent.payloadMessage(payload);
    return typeof message === 'string' ? message : null;
  }

  /** Obtiene la propiedad message cuando el cuerpo es un objeto. */
  private static payloadMessage(payload: unknown): unknown {
    if (!payload || typeof payload !== 'object' || !('message' in payload)) return null;
    return (payload as { message?: unknown }).message;
  }
}
