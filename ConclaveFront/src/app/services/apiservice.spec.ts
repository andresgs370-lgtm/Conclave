import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Apiservice, AuthResponse, GenerationResult, ProviderKey } from './apiservice';

/** Pruebas del servicio de comunicación HTTP con el backend. */
describe('Apiservice', () => {
  const baseUrl = 'http://localhost:8080';
  let service: Apiservice;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(Apiservice);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('login envía las credenciales y devuelve la sesión', () => {
    const respuesta: AuthResponse = { token: 'abc', username: 'felipe', role: 'USER' };
    let recibido: AuthResponse | undefined;
    service.login('felipe', '1234').subscribe(r => (recibido = r));

    const req = http.expectOne(`${baseUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'felipe', password: '1234' });
    req.flush(respuesta);
    expect(recibido).toEqual(respuesta);
  });

  it('register envía los datos y espera texto', () => {
    service.register('felipe', 'f@mail.com', '1234').subscribe();
    const req = http.expectOne(`${baseUrl}/auth/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.responseType).toBe('text');
    expect(req.request.body).toEqual({ username: 'felipe', email: 'f@mail.com', password: '1234' });
    req.flush('ok');
  });

  it('verificarRegistro envía correo y código', () => {
    service.verificarRegistro('f@mail.com', '123456').subscribe();
    const req = http.expectOne(`${baseUrl}/auth/verificar-registro`);
    expect(req.request.body).toEqual({ email: 'f@mail.com', codigo: '123456' });
    req.flush('ok');
  });

  it('reenviarCodigo envía el correo', () => {
    service.reenviarCodigo('f@mail.com').subscribe();
    const req = http.expectOne(`${baseUrl}/auth/reenviar-codigo`);
    expect(req.request.body).toEqual({ email: 'f@mail.com' });
    req.flush('ok');
  });

  it('solicitarReset y confirmarReset usan sus endpoints', () => {
    service.solicitarReset('f@mail.com').subscribe();
    http.expectOne(`${baseUrl}/auth/solicitar-reset`).flush('ok');

    service.confirmarReset('f@mail.com', '123456', 'nueva').subscribe();
    const req = http.expectOne(`${baseUrl}/auth/confirmar-reset`);
    expect(req.request.body).toEqual({ email: 'f@mail.com', codigo: '123456', nuevaPassword: 'nueva' });
    req.flush('ok');
  });

  it('profile agrega el token cuando hay sesión', () => {
    localStorage.setItem('conclave_token', 'tok');
    service.profile().subscribe();
    const req = http.expectOne(`${baseUrl}/user/me`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer tok');
    req.flush({});
  });

  it('promptHistory no agrega token cuando no hay sesión', () => {
    service.promptHistory().subscribe();
    const req = http.expectOne(`${baseUrl}/history/me`);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush([]);
  });

  it('listarApiKeys consulta las keys de los proveedores', () => {
    const keys: ProviderKey[] = [
      { provider: 'CLOUDFLARE', maskedKey: '****', active: true, updatedAt: null, updatedBy: null },
    ];
    let recibido: ProviderKey[] = [];
    service.listarApiKeys().subscribe(r => (recibido = r));
    const req = http.expectOne(`${baseUrl}/admin/apikey/getall`);
    expect(req.request.method).toBe('GET');
    req.flush(keys);
    expect(recibido).toEqual(keys);
  });

  it('actualizarApiKey envía la key y el estado del proveedor', () => {
    service.actualizarApiKey('POLLINATIONS', 'nueva-key', false).subscribe();
    const req = http.expectOne(`${baseUrl}/admin/apikey/update/POLLINATIONS`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ newKey: 'nueva-key', active: false });
    req.flush('ok');
  });

  it('generate envía null en las opciones que no se indican', () => {
    const resultados: GenerationResult[] = [{ provider: 'CLOUDFLARE', status: 'SUCCESS' }];
    let recibido: GenerationResult[] = [];
    service.generate('un gato', 'IMAGE', { imageProvider: 'CLOUDFLARE' }).subscribe(r => (recibido = r));

    const req = http.expectOne(`${baseUrl}/prompt/generar`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      promptText: 'un gato',
      outputType: 'IMAGE',
      format: null,
      imageProvider: 'CLOUDFLARE',
      videoProvider: null,
      videoModel: null,
      audioProvider: null,
    });
    req.flush(resultados);
    expect(recibido).toEqual(resultados);
  });
});
