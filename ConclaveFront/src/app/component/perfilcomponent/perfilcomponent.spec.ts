import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { Perfilcomponent } from './perfilcomponent';
import '../../app.module';
import { ProviderKey } from '../../services/apiservice';

/** Pruebas del perfil del usuario, su historial y la sección de API keys del administrador. */
describe('Perfilcomponent', () => {
  const baseUrl = 'http://localhost:8080';
  const keys: ProviderKey[] = [
    { provider: 'CLOUDFLARE', maskedKey: 'abcd****', active: true, updatedAt: null, updatedBy: null },
    { provider: 'POLLINATIONS', maskedKey: '', active: false, updatedAt: null, updatedBy: null },
  ];
  let component: Perfilcomponent;
  let fixture: ComponentFixture<Perfilcomponent>;
  let http: HttpTestingController;

  /**
   * Crea el componente con el rol indicado guardado en la sesión.
   *
   * @param rol rol del usuario autenticado
   */
  async function crear(rol: string): Promise<void> {
    localStorage.setItem('conclave_token', 'tok');
    localStorage.setItem('conclave_username', 'felipe');
    localStorage.setItem('conclave_role', rol);
    await TestBed.configureTestingModule({
      declarations: [Perfilcomponent],
      imports: [FormsModule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Perfilcomponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  }

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  describe('como USER', () => {
    beforeEach(async () => {
      await crear('USER');
    });

    it('should create', () => {
      http.expectOne(`${baseUrl}/user/me`).flush({});
      http.expectOne(`${baseUrl}/history/me`).flush([]);
      expect(component).toBeTruthy();
    });

    it('carga el perfil y el historial, y no pide las API keys', () => {
      http.expectOne(`${baseUrl}/user/me`).flush({ username: 'felipe', email: 'f@mail.com', enabled: true });
      http.expectOne(`${baseUrl}/history/me`).flush([
        { id: 1, promptText: 'un gato', outputType: 'IMAGE', status: 'SUCCESS', createdAt: '2026-11-21T10:00:00' },
      ]);
      http.expectNone(`${baseUrl}/admin/apikey/getall`);

      expect(component.esAdmin).toBe(false);
      expect(component.profile.email).toBe('f@mail.com');
      expect(component.history.length).toBe(1);
      expect(component.historialConRegistros()).toBe(true);
      expect((fixture.nativeElement as HTMLElement).querySelector('.keys-section')).toBeNull();
    });

    it('muestra un mensaje cuando el backend no responde', () => {
      http.expectOne(`${baseUrl}/user/me`).flush(null, { status: 0, statusText: 'Unknown Error' });
      http.expectOne(`${baseUrl}/history/me`).flush(null, { status: 401, statusText: 'Unauthorized' });

      expect(component.profileError).toContain('No se pudo conectar');
      expect(component.historyError).toContain('sesión ha expirado');
      expect(component.historialVacio()).toBe(false);
    });

    it('los métodos de ayuda devuelven los textos esperados', () => {
      http.expectOne(`${baseUrl}/user/me`).flush({});
      http.expectOne(`${baseUrl}/history/me`).flush([]);

      expect(component.mostrarValor('', 'Sin dato')).toBe('Sin dato');
      expect(component.mostrarValor('valor', 'Sin dato')).toBe('valor');
      expect(component.mostrarFecha(null)).toBe('Fecha no disponible');
      expect(component.mostrarFecha('2026-11-21T10:30:00')).toBe('21/11/2026, 10:30');
      expect(component.cuentaActiva()).toBe('Activa');
      component.profile.enabled = false;
      expect(component.cuentaActiva()).toBe('Desactivada');
      expect(component.historialVacio()).toBe(true);
      expect(component.tieneFormato({ id: 1, format: 'png' })).toBe(true);
      expect(component.tieneFormato({ id: 2 })).toBe(false);
    });
  });

  describe('como ADMIN', () => {
    beforeEach(async () => {
      await crear('ADMIN');
      http.expectOne(`${baseUrl}/user/me`).flush({ username: 'felipe', role: 'ADMIN' });
      http.expectOne(`${baseUrl}/history/me`).flush([]);
    });

    it('carga y muestra las API keys', () => {
      http.expectOne(`${baseUrl}/admin/apikey/getall`).flush(keys);
      fixture.detectChanges();

      expect(component.esAdmin).toBe(true);
      expect(component.providerKeys.length).toBe(2);
      expect((fixture.nativeElement as HTMLElement).querySelector('.keys-section')).not.toBeNull();
    });

    it('editarKey abre el formulario y cancelarKey lo cierra', () => {
      http.expectOne(`${baseUrl}/admin/apikey/getall`).flush(keys);

      component.editarKey(keys[1]);
      expect(component.keyEditando).toBe('POLLINATIONS');
      expect(component.keyForm).toEqual({ newKey: '', active: false });

      component.cancelarKey();
      expect(component.keyEditando).toBeNull();
    });

    it('guardarKey envía la key nueva y recarga la lista', () => {
      http.expectOne(`${baseUrl}/admin/apikey/getall`).flush(keys);
      component.editarKey(keys[0]);
      component.keyForm = { newKey: '  nueva-key  ', active: true };

      component.guardarKey();
      const req = http.expectOne(`${baseUrl}/admin/apikey/update/CLOUDFLARE`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ newKey: 'nueva-key', active: true });
      req.flush('ok');

      http.expectOne(`${baseUrl}/admin/apikey/getall`).flush(keys);
      expect(component.keyEditando).toBeNull();
      expect(component.keyMensaje).toContain('CLOUDFLARE');
      expect(component.guardandoKey).toBe(false);
    });

    it('guardarKey envía null si el campo queda vacío', () => {
      http.expectOne(`${baseUrl}/admin/apikey/getall`).flush(keys);
      component.editarKey(keys[0]);
      component.keyForm = { newKey: '   ', active: false };

      component.guardarKey();
      const req = http.expectOne(`${baseUrl}/admin/apikey/update/CLOUDFLARE`);
      expect(req.request.body).toEqual({ newKey: null, active: false });
      req.flush('ok');
      http.expectOne(`${baseUrl}/admin/apikey/getall`).flush(keys);
    });

    it('guardarKey muestra un error si el backend falla', () => {
      http.expectOne(`${baseUrl}/admin/apikey/getall`).flush(keys);
      component.editarKey(keys[0]);

      component.guardarKey();
      http.expectOne(`${baseUrl}/admin/apikey/update/CLOUDFLARE`).flush('error', { status: 500, statusText: 'Error' });

      expect(component.keyError).toBe('No se pudo actualizar la API key.');
      expect(component.guardandoKey).toBe(false);
    });

    it('con 403 indica que solo los administradores pueden configurar las keys', () => {
      http.expectOne(`${baseUrl}/admin/apikey/getall`).flush(null, { status: 403, statusText: 'Forbidden' });

      expect(component.providerKeys.length).toBe(0);
      expect(component.keyError).toContain('Solo los administradores');
    });
  });
});
