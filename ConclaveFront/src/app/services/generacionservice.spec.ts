import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { GeneracionService, TipoGeneracion } from './generacionservice';

/** Pruebas del servicio global de generación asíncrona. */
describe('GeneracionService', () => {
  const url = 'http://localhost:8080/prompt/generar';
  let service: GeneracionService;
  let http: HttpTestingController;
  let router: Router;

  /**
   * Simula que el usuario está en una ruta concreta.
   *
   * @param ruta ruta actual simulada
   */
  function estarEn(ruta: string): void {
    vi.spyOn(router, 'url', 'get').mockReturnValue(ruta);
  }

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(GeneracionService);
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    http.verify();
    vi.clearAllTimers();
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('empieza con estados vacíos', () => {
    for (const tipo of ['IMAGE', 'VIDEO', 'AUDIO'] as TipoGeneracion[]) {
      expect(service.estado(tipo)).toEqual({ prompt: '', cargando: false, resultados: [], error: '' });
    }
  });

  it('generar marca el estado como cargando y avisa el cambio', () => {
    const cambios: TipoGeneracion[] = [];
    service.cambios$.subscribe(tipo => cambios.push(tipo));

    service.generar('IMAGE', 'un gato', { imageProvider: 'CLOUDFLARE' });

    expect(service.estado('IMAGE').cargando).toBe(true);
    expect(service.estado('IMAGE').prompt).toBe('un gato');
    expect(cambios).toEqual(['IMAGE']);
    const req = http.expectOne(url);
    expect(req.request.body.imageProvider).toBe('CLOUDFLARE');
    req.flush([]);
  });

  it('guarda el resultado y no muestra toast si el usuario sigue en la página', () => {
    estarEn('/imagen');
    service.generar('IMAGE', 'un gato', {});
    http.expectOne(url).flush([{ provider: 'CLOUDFLARE', status: 'SUCCESS', downloadUrl: 'data:image/png;base64,AA' }]);

    const estado = service.estado('IMAGE');
    expect(estado.cargando).toBe(false);
    expect(estado.error).toBe('');
    expect(estado.resultados.length).toBe(1);
    expect(service.toasts().length).toBe(0);
  });

  it('muestra un toast de éxito si el usuario está en otra página', () => {
    estarEn('/perfil');
    service.generar('AUDIO', 'hola', { audioProvider: 'ELEVENLABS' });
    http.expectOne(url).flush([{ provider: 'ELEVENLABS', status: 'SUCCESS' }]);

    expect(service.toasts().length).toBe(1);
    expect(service.toasts()[0].exito).toBe(true);
    expect(service.toasts()[0].ruta).toBe('/sonido');
  });

  it('con error 500 guarda el mensaje de la IA y muestra un toast de fallo', () => {
    estarEn('/');
    service.generar('VIDEO', 'un perro', {});
    http.expectOne(url).flush(
      [{ provider: 'HF_MINIMAX_H3', status: 'FAILED', errorMessage: 'Cuota agotada' }],
      { status: 500, statusText: 'Internal Server Error' },
    );

    expect(service.estado('VIDEO').error).toBe('Cuota agotada');
    expect(service.toasts()[0].exito).toBe(false);
    expect(service.toasts()[0].ruta).toBe('/video');
  });

  it('con error 503 indica que falta la API key', () => {
    estarEn('/imagen');
    service.generar('IMAGE', 'un gato', {});
    http.expectOne(url).flush(null, { status: 503, statusText: 'Service Unavailable' });

    expect(service.estado('IMAGE').error).toContain('API key');
  });

  it('el toast se cierra solo después de unos segundos', () => {
    estarEn('/');
    service.generar('IMAGE', 'un gato', {});
    http.expectOne(url).flush([{ status: 'SUCCESS' }]);
    expect(service.toasts().length).toBe(1);

    vi.advanceTimersByTime(8000);
    expect(service.toasts().length).toBe(0);
  });

  it('cerrarToast quita el toast indicado', () => {
    estarEn('/');
    service.generar('IMAGE', 'un gato', {});
    http.expectOne(url).flush([{ status: 'SUCCESS' }]);

    service.cerrarToast(service.toasts()[0].id);
    expect(service.toasts().length).toBe(0);
  });

  it('limpiar reinicia el estado del tipo indicado', () => {
    estarEn('/imagen');
    service.generar('IMAGE', 'un gato', {});
    http.expectOne(url).flush([{ status: 'SUCCESS' }]);

    service.limpiar('IMAGE');
    expect(service.estado('IMAGE')).toEqual({ prompt: '', cargando: false, resultados: [], error: '' });
  });
});
