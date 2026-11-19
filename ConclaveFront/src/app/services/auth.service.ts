import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { AuthResponse } from './api.service';

/** Gestiona la sesión local y publica el nombre del usuario autenticado. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenStorageKey = 'conclave_token';
  private readonly usernameSubject = new BehaviorSubject<string | null>(localStorage.getItem('conclave_username'));
  readonly username$ = this.usernameSubject.asObservable();

  /** Guarda los datos de sesión recibidos del backend. */
  setSession(response: AuthResponse): void {
    localStorage.setItem(this.tokenStorageKey, response.token);
    localStorage.setItem('conclave_username', response.username);
    localStorage.setItem('conclave_role', response.role ?? 'USER');
    this.usernameSubject.next(response.username);
  }

  /** Elimina la sesión local y notifica que no hay usuario autenticado. */
  logout(): void {
    localStorage.removeItem(this.tokenStorageKey);
    localStorage.removeItem('conclave_username');
    localStorage.removeItem('conclave_role');
    this.usernameSubject.next(null);
  }

  /** Indica si hay token y un nombre de usuario en la sesión local. */
  get isLoggedIn(): boolean {
    return Boolean(localStorage.getItem(this.tokenStorageKey));
  }
}
