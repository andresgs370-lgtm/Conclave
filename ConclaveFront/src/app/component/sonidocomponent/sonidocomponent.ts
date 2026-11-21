import { Component, OnDestroy } from '@angular/core';
import { Subscription, filter } from 'rxjs';
import { GenerationResult } from '../../services/apiservice';
import { GeneracionService } from '../../services/generacionservice';

/**
 * Componente para generar y reproducir audio. La generación la ejecuta
 * {@link GeneracionService}, así que continúa aunque el usuario cambie de página.
 */
@Component({
  selector: 'app-sonidocomponent',
  standalone: false,
  styleUrl: './sonidocomponent.css',
  templateUrl: './sonidocomponent.html',
})
export class Sonidocomponent implements OnDestroy {
  prompt = '';
  results: GenerationResult[] = [];
  error = '';
  loading = false;
  format = 'WAV';

  /** Proveedores de voz disponibles. */
  readonly audioProviders: { value: string; label: string }[] = [
    { value: 'GEMINI', label: 'Google Gemini — TTS' },
    { value: 'CLOUDFLARE', label: 'Cloudflare — Deepgram Aura (gratis, voz en español)' },
    { value: 'ELEVENLABS', label: 'ElevenLabs — Multilingual v2 (gratis, ~10 min/mes)' },
  ];

  /** Proveedor de voz seleccionado. */
  audioProvider = this.audioProviders[0].value;
  loginRequired = !localStorage.getItem('conclave_token');

  /** Suscripción a los cambios de estado de la generación de audio. */
  private readonly suscripcion: Subscription;

  /**
   * Recupera la última generación de audio y escucha sus cambios.
   *
   * @param generacion servicio global de generación
   */
  constructor(private generacion: GeneracionService) {
    this.prompt = generacion.estado('AUDIO').prompt;
    this.sincronizar();
    this.suscripcion = generacion.cambios$
      .pipe(filter(tipo => tipo === 'AUDIO'))
      .subscribe(() => this.sincronizar());
  }

  /** Deja de escuchar los cambios al salir de la página; la generación sigue en curso. */
  ngOnDestroy(): void {
    this.suscripcion.unsubscribe();
  }

  /** Abre los enlaces de descarga de los audios generados. */
  downloadAll(): void {
    this.results.filter(result => result.downloadUrl).forEach(result => window.open(result.downloadUrl, '_blank', 'noopener'));
  }

  /** Comparte el texto o lo copia si Web Share no está disponible. */
  sharePrompt(): void {
    const shareText = this.prompt;
    if (navigator.share) {
      navigator.share({ title: 'Prompt de Conclave', text: shareText }).catch(() => undefined);
    } else {
      navigator.clipboard.writeText(shareText).catch(() => { this.error = 'No fue posible copiar el prompt.'; });
    }
  }

  /** Valida el prompt y solicita la generación de audio. */
  generate(): void {
    this.error = this.validationError();
    if (this.error) return;
    this.generacion.generar('AUDIO', this.prompt, { format: this.format, audioProvider: this.audioProvider });
  }

  /** Copia el estado guardado en el servicio a los campos que muestra la vista. */
  private sincronizar(): void {
    const estado = this.generacion.estado('AUDIO');
    this.loading = estado.cargando;
    this.error = estado.error;
    this.results = estado.resultados;
  }

  /** Comprueba que el prompt exista y que el usuario tenga una sesión activa. */
  private validationError(): string {
    if (!this.prompt.trim()) return 'Escribe un prompt para continuar.';
    if (!localStorage.getItem('conclave_token')) return 'Inicia sesión para generar contenido.';
    return '';
  }
}
