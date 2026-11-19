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

/** Component for requesting and displaying generated images. */
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

  downloadAll(): void {
    this.results
      .filter((result) => Boolean(result.downloadUrl))
      .forEach((result) => window.open(result.downloadUrl, '_blank', 'noopener'));
  }

  sharePrompt(): void {
    const shareText = this.prompt;
    if (navigator.share) {
      navigator.share({ title: 'Prompt de Conclave', text: shareText }).catch(() => {
        // Sharing can be cancelled by the user.
      });
    } else {
      void navigator.clipboard.writeText(shareText);
    }
  }

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
        this.results = this.normalizeResults(response);
        this.loading = false;
      },
      error: (err: unknown) => {
        this.error = this.getErrorMessage(err);
        this.loading = false;
      },
    });
  }

  private normalizeResults(response: unknown): GenerationResult[] {
    if (!Array.isArray(response)) return [];
    return response.map((item: unknown) => {
      const result = item as Omit<GenerationResult, 'mediaType' | 'statusClass'>;
      const mimeType = result.mimeType || '';
      return {
        ...result,
        mediaType: this.getMediaType(mimeType),
        statusClass: result.status === 'SUCCESS' ? 'bg-success' : 'bg-danger',
      };
    });
  }

  private getMediaType(mimeType: string): GeneratedMediaType {
    if (mimeType.startsWith('image/')) return 'image';
    if (mimeType.startsWith('video/')) return 'video';
    if (mimeType.startsWith('audio/')) return 'audio';
    return 'other';
  }

  private getErrorMessage(error: unknown): string {
    if (typeof error === 'string') return error;
    if (error && typeof error === 'object' && 'error' in error) {
      const payload = (error as { error?: unknown }).error;
      if (typeof payload === 'string') return payload;
      if (payload && typeof payload === 'object' && 'message' in payload) {
        const message = (payload as { message?: unknown }).message;
        if (typeof message === 'string') return message;
      }
    }
    return 'No fue posible generar el contenido.';
  }
}
