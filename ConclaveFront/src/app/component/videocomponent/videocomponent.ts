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
    this.api.generate(this.prompt, 'VIDEO', this.format).subscribe({
      next: (response) => { this.results = Array.isArray(response) ? response : []; this.loading = false; },
      error: (err) => { this.error = typeof err.error === 'string' ? err.error : err.error?.message || 'No fue posible generar el contenido.'; this.loading = false; }
    });
  }
}
