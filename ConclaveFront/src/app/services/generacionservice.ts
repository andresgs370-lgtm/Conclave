import { ApplicationRef, Injectable, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { Apiservice, GenerationResult } from './apiservice';

/** Tipos de contenido que se pueden generar. */
export type TipoGeneracion = 'IMAGE' | 'VIDEO' | 'AUDIO';

/** Estado en memoria de la última generación de un tipo de contenido. */
export interface EstadoGeneracion {
  prompt: string;
  cargando: boolean;
  resultados: GenerationResult[];
  error: string;
}

/** Aviso emergente que se muestra cuando una generación termina. */
export interface Toast {
  id: number;
  mensaje: string;
  ruta: string;
  exito: boolean;
}

/** Opciones de proveedor y formato de una generación. */
export interface OpcionesGeneracion {
  format?: string;
  imageProvider?: string;
  videoProvider?: string;
  audioProvider?: string;
}

/**
 * Servicio global que ejecuta las generaciones de forma asíncrona y conserva su estado
 * mientras el usuario navega por la aplicación.
 *
 * <p>Cuando una promesa se cumple o se rechaza y el usuario está en otra página, muestra
 * un toast con un enlace al resultado. El contenido multimedia solo vive en memoria del
 * navegador: no se guarda en la base de datos y se pierde al recargar la página.</p>
 */
@Injectable({ providedIn: 'root' })
export class GeneracionService {

  /** Ruta y nombre visible de cada tipo de contenido. */
  private static readonly info: Record<TipoGeneracion, { ruta: string; nombre: string }> = {
    IMAGE: { ruta: '/imagen', nombre: 'imagen' },
    VIDEO: { ruta: '/video', nombre: 'video' },
    AUDIO: { ruta: '/sonido', nombre: 'audio' },
  };

  /** Milisegundos que permanece visible un toast. */
  private static readonly duracionToast = 8000;

  /** Estado de la última generación de cada tipo. */
  private readonly estados: Record<TipoGeneracion, EstadoGeneracion> = {
    IMAGE: GeneracionService.estadoVacio(),
    VIDEO: GeneracionService.estadoVacio(),
    AUDIO: GeneracionService.estadoVacio(),
  };

  /** Avisa a los componentes qué tipo de generación cambió de estado. */
  readonly cambios$ = new Subject<TipoGeneracion>();

  /**
   * Toasts visibles actualmente. Es un signal para que el componente de avisos se repinte
   * solo, aunque la aplicación no use zone.js y los componentes sean OnPush.
   */
  readonly toasts = signal<Toast[]>([]);

  /** Identificador incremental de los toasts. */
  private siguienteId = 1;

  /**
   * Inyecta el servicio de API, el enrutador y la referencia a la aplicación.
   *
   * @param api servicio de comunicación con el backend
   * @param router enrutador, para saber en qué página está el usuario
   * @param appRef referencia a la aplicación, para refrescar la vista
   */
  constructor(private api: Apiservice, private router: Router, private appRef: ApplicationRef) {}

  /**
   * Devuelve el estado actual de un tipo de generación.
   *
   * @param tipo tipo de contenido
   * @returns estado de la última generación de ese tipo
   */
  estado(tipo: TipoGeneracion): EstadoGeneracion {
    return this.estados[tipo];
  }

  /**
   * Lanza una generación asíncrona. La petición sigue en curso aunque el usuario cambie
   * de página; al terminar se actualiza el estado y, si hace falta, se muestra un toast.
   *
   * @param tipo tipo de contenido a generar
   * @param prompt texto del usuario
   * @param opciones proveedor y formato elegidos
   */
  generar(tipo: TipoGeneracion, prompt: string, opciones: OpcionesGeneracion): void {
    const estado = this.estados[tipo];
    estado.prompt = prompt;
    estado.cargando = true;
    estado.resultados = [];
    estado.error = '';
    this.notificar(tipo);
    this.api.generate(prompt, tipo, opciones).subscribe({
      next: respuesta => this.terminar(tipo, Array.isArray(respuesta) ? respuesta : [], ''),
      error: (e: HttpErrorResponse) => this.terminar(tipo, Array.isArray(e.error) ? e.error : [],
        GeneracionService.mensajeError(e)),
    });
  }

  /**
   * Borra el estado de un tipo de generación.
   *
   * @param tipo tipo de contenido
   */
  limpiar(tipo: TipoGeneracion): void {
    this.estados[tipo] = GeneracionService.estadoVacio();
    this.notificar(tipo);
  }

  /**
   * Cierra un toast antes de que desaparezca solo.
   *
   * @param id identificador del toast
   */
  cerrarToast(id: number): void {
    this.toasts.update(lista => lista.filter(toast => toast.id !== id));
    this.refrescar();
  }

  /**
   * Guarda el resultado de una generación y avisa al usuario si está en otra página.
   *
   * @param tipo tipo de contenido
   * @param resultados respuestas de las IA
   * @param error mensaje de error, vacío si la petición respondió correctamente
   */
  private terminar(tipo: TipoGeneracion, resultados: GenerationResult[], error: string): void {
    const exito = resultados.some(resultado => resultado.status === 'SUCCESS');
    const estado = this.estados[tipo];
    estado.cargando = false;
    estado.resultados = resultados;
    estado.error = exito ? '' : GeneracionService.mensajeFallo(resultados, error);
    this.avisarSiEstaFuera(tipo, exito);
    this.notificar(tipo);
  }

  /**
   * Elige el mensaje de error de una generación fallida.
   *
   * @param resultados respuestas de las IA
   * @param error mensaje de error de la petición
   * @returns mensaje de la IA, de la petición o uno genérico, en ese orden
   */
  private static mensajeFallo(resultados: GenerationResult[], error: string): string {
    const fallo = resultados.find(resultado => resultado.status !== 'SUCCESS');
    return [fallo?.errorMessage, error].find(Boolean) ?? 'La generación no pudo completarse.';
  }

  /**
   * Muestra un toast si el usuario no está en la página del tipo de contenido generado.
   *
   * @param tipo tipo de contenido
   * @param exito indica si la generación terminó correctamente
   */
  private avisarSiEstaFuera(tipo: TipoGeneracion, exito: boolean): void {
    const { ruta, nombre } = GeneracionService.info[tipo];
    if (this.router.url.startsWith(ruta)) return;
    const mensaje = exito ? `Tu ${nombre} ya está lista.` : `No se pudo generar tu ${nombre}.`;
    this.mostrarToast(mensaje, ruta, exito);
  }

  /**
   * Muestra un toast durante unos segundos.
   *
   * @param mensaje texto del aviso
   * @param ruta página a la que lleva el enlace del aviso
   * @param exito indica si la generación terminó correctamente
   */
  private mostrarToast(mensaje: string, ruta: string, exito: boolean): void {
    const id = this.siguienteId++;
    this.toasts.update(lista => [...lista, { id, mensaje, ruta, exito }]);
    setTimeout(() => this.cerrarToast(id), GeneracionService.duracionToast);
  }

  /**
   * Avisa a los componentes del cambio y refresca la vista.
   *
   * @param tipo tipo de contenido que cambió
   */
  private notificar(tipo: TipoGeneracion): void {
    this.cambios$.next(tipo);
    this.refrescar();
  }

  /** Programa la detección de cambios, necesaria porque la aplicación no usa zone.js. */
  private refrescar(): void {
    setTimeout(() => this.appRef.tick());
  }

  /**
   * Crea un estado vacío.
   *
   * @returns estado sin generación
   */
  private static estadoVacio(): EstadoGeneracion {
    return { prompt: '', cargando: false, resultados: [], error: '' };
  }

  /**
   * Convierte un error HTTP en un mensaje legible.
   *
   * @param e error de la petición
   * @returns mensaje de error
   */
  private static mensajeError(e: HttpErrorResponse): string {
    return GeneracionService.mensajesPorEstado[e.status] ?? GeneracionService.mensajeDelBackend(e);
  }

  /** Mensajes fijos para los códigos HTTP más comunes. */
  private static readonly mensajesPorEstado: Record<number, string> = {
    0: 'No fue posible conectar con el backend.',
    503: 'Falta configurar la API key del proveedor en el panel de administración.',
  };

  /**
   * Extrae el mensaje que envió el backend, como texto o en el campo {@code message}.
   *
   * @param e error de la petición
   * @returns mensaje del backend o el código HTTP
   */
  private static mensajeDelBackend(e: HttpErrorResponse): string {
    const cuerpo = e.error as string | { message?: unknown } | null;
    const mensaje = typeof cuerpo === 'string' ? cuerpo.trim() : cuerpo?.message;
    return typeof mensaje === 'string' && mensaje ? mensaje : `Error HTTP ${e.status}`;
  }
}
