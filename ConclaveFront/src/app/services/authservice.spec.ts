import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { Authservice } from './authservice';

/** Pruebas del servicio de sesión local. */
describe('Authservice', () => {
  let service: Authservice;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    service = TestBed.inject(Authservice);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('no hay sesión al inicio', () => {
    expect(service.isLoggedIn).toBe(false);
    expect(service.token).toBeNull();
    expect(service.isAdmin).toBe(false);
  });

  it('setSession guarda el token, el usuario y el rol', async () => {
    service.setSession({ token: 'tok', username: 'felipe', role: 'ADMIN' });

    expect(localStorage.getItem('conclave_token')).toBe('tok');
    expect(localStorage.getItem('conclave_username')).toBe('felipe');
    expect(localStorage.getItem('conclave_role')).toBe('ADMIN');
    expect(service.isLoggedIn).toBe(true);
    expect(service.isAdmin).toBe(true);
    expect(await firstValueFrom(service.username$)).toBe('felipe');
    expect(await firstValueFrom(service.isAdmin$)).toBe(true);
  });

  it('setSession asigna USER cuando el backend no envía rol', async () => {
    service.setSession({ token: 'tok', username: 'ana' });

    expect(localStorage.getItem('conclave_role')).toBe('USER');
    expect(service.isAdmin).toBe(false);
    expect(await firstValueFrom(service.isAdmin$)).toBe(false);
  });

  it('reconoce el rol ROLE_ADMIN sin importar mayúsculas', () => {
    service.setSession({ token: 'tok', username: 'felipe', role: 'role_admin' });
    expect(service.isAdmin).toBe(true);
  });

  it('logout borra la sesión', async () => {
    service.setSession({ token: 'tok', username: 'felipe', role: 'ADMIN' });
    service.logout();

    expect(localStorage.getItem('conclave_token')).toBeNull();
    expect(localStorage.getItem('conclave_username')).toBeNull();
    expect(localStorage.getItem('conclave_role')).toBeNull();
    expect(service.isLoggedIn).toBe(false);
    expect(service.isAdmin).toBe(false);
    expect(await firstValueFrom(service.username$)).toBeNull();
  });
});
