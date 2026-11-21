import { Component, OnDestroy } from '@angular/core';
import { Subscription, filter } from 'rxjs';
import { GenerationResult } from '../../services/apiservice';
import { GeneracionService } from '../../services/generacionservice';

/**
 * Componente para solicitar y visualizar videos generados. La generación la ejecuta
 * {@link GeneracionService}, así que continúa aunque el usuario cambie de página.
 */
@Component({
  selector: 'app-videocomponent',
  standalone: false,
  styleUrl: './videocomponent.css',
  templateUrl: './videocomponent.html',
})
export class Videocomponent implements OnDestroy {
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

  loginRequired = !localStorage.getItem('conclave_token');

  /** Suscripción a los cambios de estado de la generación de video. */
  private readonly suscripcion: Subscription;

  /**
   * Recupera la última generación de video y escucha sus cambios.
   *
   * @param generacion servicio global de generación
   */
  constructor(private generacion: GeneracionService) {
    this.prompt = generacion.estado('VIDEO').prompt;
    this.sincronizar();
    this.suscripcion = generacion.cambios$
      .pipe(filter(tipo => tipo === 'VIDEO'))
      .subscribe(() => this.sincronizar());
  }

  /** Deja de escuchar los cambios al salir de la página; la generación sigue en curso. */
  ngOnDestroy(): void {
    this.suscripcion.unsubscribe();
  }

  /** Abre los enlaces de descarga de los resultados disponibles. */
  downloadAll(): void {
    this.results.filter(result => Boolean(result.downloadUrl)).forEach(result => window.open(result.downloadUrl, '_blank', 'noopener'));
  }

  /** Comparte el prompt o lo copia si compartir no está disponible. */
  sharePrompt(): void {
    const shareText = this.prompt;
    if (navigator.share) {
      navigator.share({ title: 'Prompt de Conclave', text: shareText }).catch(() => undefined);
    } else {
      navigator.clipboard.writeText(shareText).catch(() => { this.error = 'No fue posible copiar el prompt.'; });
    }
  }

  /** Valida el prompt y solicita la generación del video. */
  generate(): void {
    this.error = '';
    if (!this.prompt.trim()) {
      this.error = 'Escribe un prompt para continuar.';
      return;
    }
    if (!localStorage.getItem('conclave_token')) {
      this.error = 'Inicia sesión para generar contenido.';
      return;
    }
    this.generacion.generar('VIDEO', this.prompt, { format: this.format, videoProvider: this.videoProvider });
  }

  /** Copia el estado guardado en el servicio a los campos que muestra la vista. */
  private sincronizar(): void {
    const estado = this.generacion.estado('VIDEO');
    this.loading = estado.cargando;
    this.error = estado.error;
    this.results = estado.resultados;
  }
}
