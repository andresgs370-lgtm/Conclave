import { Component } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-sonidocomponent',
  standalone: false,
  styleUrl: './sonidocomponent.css',
  templateUrl: './sonidocomponent.html',
})
export class Sonidocomponent {
  prompt = '';
  results: any[] = [];
  error = '';
  loading = false;
  format = 'MP3';
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
    this.api.generate(this.prompt, 'AUDIO', this.format).subscribe({
      next: (response) => { this.results = Array.isArray(response) ? response : []; this.loading = false; },
      error: (err) => { this.error = typeof err.error === 'string' ? err.error : err.error?.message || 'No fue posible generar el contenido.'; this.loading = false; }
    });
  }
}
