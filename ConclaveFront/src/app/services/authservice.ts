import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthResponse } from './apiservice';

/** Gestiona la sesión local y publica el nombre y el rol del usuario autenticado. */
@Injectable({ providedIn: 'root' })
export class Authservice {
  private readonly tokenStorageKey = 'conclave_token';
  private readonly usernameSubject = new BehaviorSubject<string | null>(localStorage.getItem('conclave_username'));
  private readonly roleSubject = new BehaviorSubject<string | null>(localStorage.getItem('conclave_role'));

  /** Nombre del usuario autenticado, o `null` si no hay sesión. */
  readonly username$ = this.usernameSubject.asObservable();

  /** Emite `true` mientras el usuario autenticado tenga rol ADMIN. */
  readonly isAdmin$ = this.roleSubject.pipe(
    map(role => (role ?? '').toUpperCase().includes('ADMIN'))
  );

  /** Guarda los datos de sesión recibidos del backend. */
  setSession(response: AuthResponse): void {
    localStorage.setItem(this.tokenStorageKey, response.token);
    localStorage.setItem('conclave_username', response.username);
    localStorage.setItem('conclave_role', response.role ?? 'USER');
    this.usernameSubject.next(response.username);
    this.roleSubject.next(response.role ?? 'USER');
  }

  /** Elimina la sesión local y notifica que no hay usuario autenticado. */
  logout(): void {
    localStorage.removeItem(this.tokenStorageKey);
    localStorage.removeItem('conclave_username');
    localStorage.removeItem('conclave_role');
    this.usernameSubject.next(null);
    this.roleSubject.next(null);
  }

  /** Token de la sesión local, o `null` si no hay sesión. */
  get token(): string | null {
    return localStorage.getItem(this.tokenStorageKey);
  }

  /** Indica si hay token en la sesión local. */
  get isLoggedIn(): boolean {
    return Boolean(this.token);
  }

  /** Indica si el rol de la sesión actual es ADMIN. */
  get isAdmin(): boolean {
    return (this.roleSubject.value ?? '').toUpperCase().includes('ADMIN');
  }
}
