import { Component } from '@angular/core';
import { ApiService, GenerationResult } from '../../services/api.service';

/** Componente para generar y reproducir audio. */
@Component({
  selector: 'app-sonidocomponent',
  standalone: false,
  styleUrl: './sonidocomponent.css',
  templateUrl: './sonidocomponent.html',
})
export class Sonidocomponent {
  prompt = '';
  results: GenerationResult[] = [];
  error = '';
  loading = false;
  format = 'WAV';
  loginRequired = !localStorage.getItem('conclave_token');

  constructor(private api: ApiService) {}

  /** Abre los enlaces de descarga de los audios generados. */
  downloadAll(): void {
    this.results.filter(result => result.downloadUrl).forEach(result => window.open(result.downloadUrl, '_blank', 'noopener'));
  }

  /** Comparte el texto o lo copia si Web Share no está disponible. */
  sharePrompt(): void {
    const shareText = this.prompt;
    if (navigator.share) {
      navigator.share({ title: 'Prompt de Conclave', text: shareText }).catch(() => { this.error = 'No fue posible compartir el prompt.'; });
    } else {
      navigator.clipboard.writeText(shareText).catch(() => { this.error = 'No fue posible copiar el prompt.'; });
    }
  }

  /** Valida el prompt y solicita la generación de audio. */
  generate(): void {
    this.error = '';
    this.results = [];
    const validationError = this.validationError();
    if (validationError) {
      this.error = validationError;
      return;
    }
    this.loading = true;
    this.api.generate(this.prompt, 'AUDIO', this.format).subscribe({
      next: (response) => { this.results = Array.isArray(response) ? response : []; this.loading = false; },
      error: (err) => { this.error = typeof err.error === 'string' ? err.error : err.error?.message || 'No fue posible generar el contenido.'; this.loading = false; }
    });
  }

  /** Comprueba que el prompt exista y que el usuario tenga una sesión activa. */
  private validationError(): string {
    if (!this.prompt.trim()) return 'Escribe un prompt para continuar.';
    if (!localStorage.getItem('conclave_token')) return 'Inicia sesión para generar contenido.';
    return '';
  }
}
