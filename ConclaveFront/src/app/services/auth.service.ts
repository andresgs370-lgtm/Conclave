import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly usernameSubject = new BehaviorSubject<string | null>(localStorage.getItem('conclave_username'));
  readonly username$ = this.usernameSubject.asObservable();

  setSession(response: any): void {
    localStorage.setItem('conclave_token', response.token);
    localStorage.setItem('conclave_username', response.username);
    localStorage.setItem('conclave_role', response.role ?? 'USER');
    this.usernameSubject.next(response.username);
  }

  logout(): void {
    localStorage.removeItem('conclave_token');
    localStorage.removeItem('conclave_username');
    localStorage.removeItem('conclave_role');
    this.usernameSubject.next(null);
  }

  get isLoggedIn(): boolean {
    return !!localStorage.getItem('conclave_token');
  }
}
