import { Component } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-videocomponent',
  standalone: false,
  styleUrl: './videocomponent.css',
  templateUrl: './videocomponent.html',
})
export class Videocomponent {
  prompt = '';
  results: any[] = [];
  error = '';
  loading = false;
  format = 'MP4';
  videoProvider = 'AGNES_AI';
  videoModel = '';

  modelPlaceholder(): string {
    if (this.videoProvider === 'FAL_AI') return 'fal-ai/wan/v2.1/1.3b/text-to-video';
    if (this.videoProvider === 'KINOVI_AI') return 'seedance2-5';
    if (this.videoProvider === 'REPLICATE') return 'owner/model';
    if (this.videoProvider === 'HUGGINGFACE') return 'organization/space-name';
    return 'agnes-video-2.5-flash';
  }
  loginRequired = !localStorage.getItem('conclave_token');

  constructor(private api: ApiService) {}

  downloadAll(): void {
    this.results.filter(result => result.downloadUrl).forEach(result => window.open(result.downloadUrl, '_blank', 'noopener'));
  }

  sharePrompt(): void {
    const shareText = this.prompt;
    if (navigator.share) navigator.share({ title: 'Prompt de Conclave', text: shareText }).catch(() => {});
    else navigator.clipboard.writeText(shareText);
  }

  private extractError(err: any): string {
    if (err?.status === 503) return 'Falta configurar o activar una API key requerida en el backend. Agnes AI debe estar configurado en Swagger.';
    const body = err?.error;
    if (typeof body === 'string' && body.trim()) return body;
    if (body?.message) return body.message;
    if (body?.error?.message) return body.error.message;
    if (body?.detail) return body.detail;
    if (body?.error) return typeof body.error === 'string' ? body.error : JSON.stringify(body.error);
    if (err?.message) return err.message;
    if (err?.status) return `Error HTTP ${err.status}: ${err.statusText || 'La solicitud fue rechazada por el servidor.'}`;
    return 'No fue posible generar el contenido. Revisa la conexión con el backend.';
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
    this.api.generate(this.prompt, 'VIDEO', this.format, undefined, this.videoProvider, this.videoModel.trim() || this.modelPlaceholder()).subscribe({
      next: (response) => { this.results = Array.isArray(response) ? response : []; this.loading = false; if (this.results.some(result => result.status !== 'SUCCESS')) this.error = this.results.find(result => result.errorMessage)?.errorMessage || 'La generación no pudo completarse.'; },
      error: (err) => { this.error = this.extractError(err); this.loading = false; }
    });
  }
}
