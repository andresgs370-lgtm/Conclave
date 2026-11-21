import { Component, OnDestroy } from '@angular/core';
import { Subscription, filter } from 'rxjs';
import { GenerationResult as ApiGenerationResult } from '../../services/apiservice';
import { GeneracionService } from '../../services/generacionservice';

type GeneratedMediaType = 'image' | 'video' | 'audio' | 'other';

interface GenerationResult extends ApiGenerationResult {
  mediaType: GeneratedMediaType;
  statusClass: string;
}

/**
 * Componente para generar y visualizar imágenes. La generación la ejecuta
 * {@link GeneracionService}, así que continúa aunque el usuario cambie de página.
 */
@Component({
  selector: 'app-imagencomponent',
  standalone: false,
  styleUrl: './imagencomponent.css',
  templateUrl: './imagencomponent.html',
})
export class Imagencomponent implements OnDestroy {
  prompt = '';
  results: GenerationResult[] = [];
  error = '';
  loading = false;
  format = 'PNG';
  imageProvider = 'CLOUDFLARE';
  loginRequired = !localStorage.getItem('conclave_token');

  /** Suscripción a los cambios de estado de la generación de imágenes. */
  private readonly suscripcion: Subscription;

  /**
   * Recupera la última generación de imágenes y escucha sus cambios.
   *
   * @param generacion servicio global de generación
   */
  constructor(private generacion: GeneracionService) {
    this.prompt = generacion.estado('IMAGE').prompt;
    this.sincronizar();
    this.suscripcion = generacion.cambios$
      .pipe(filter(tipo => tipo === 'IMAGE'))
      .subscribe(() => this.sincronizar());
  }

  /** Deja de escuchar los cambios al salir de la página; la generación sigue en curso. */
  ngOnDestroy(): void {
    this.suscripcion.unsubscribe();
  }

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
      navigator.share({ title: 'Prompt de Conclave', text: shareText }).catch(() => undefined);
    } else {
      navigator.clipboard.writeText(shareText).catch(() => { this.error = 'No fue posible copiar el prompt.'; });
    }
  }

  /** Limpia el formulario y el último resultado. */
  nuevaGeneracion(): void {
    this.prompt = '';
    this.generacion.limpiar('IMAGE');
  }

  /** Valida la sesión y solicita la generación de imágenes. */
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
    this.generacion.generar('IMAGE', this.prompt, { format: this.format, imageProvider: this.imageProvider });
  }

  /** Copia el estado guardado en el servicio a los campos que muestra la vista. */
  private sincronizar(): void {
    const estado = this.generacion.estado('IMAGE');
    this.loading = estado.cargando;
    this.error = estado.error;
    this.results = Imagencomponent.normalizeResults(estado.resultados);
  }

  /** Agrega el tipo de contenido y la clase de estado a cada resultado. */
  private static normalizeResults(response: ApiGenerationResult[]): GenerationResult[] {
    return response.map(result => ({
      ...result,
      mediaType: Imagencomponent.getMediaType(result.mimeType || ''),
      statusClass: result.status === 'SUCCESS' ? 'bg-success' : 'bg-danger',
    }));
  }

  /** Determina el tipo de contenido a partir del MIME recibido. */
  private static getMediaType(mimeType: string): GeneratedMediaType {
    if (mimeType.startsWith('image/')) return 'image';
    if (mimeType.startsWith('video/')) return 'video';
    if (mimeType.startsWith('audio/')) return 'audio';
    return 'other';
  }
}
