import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AuthResponse {
  token: string;
  username: string;
  role?: string;
}

export interface GenerationResult {
  provider?: string;
  model?: string;
  status?: string;
  textContent?: string;
  errorMessage?: string;
  downloadUrl?: string;
  mimeType?: string;
  durationMs?: number;
  httpStatus?: number;
  format?: string;
}

/** Servicio para comunicación HTTP con el backend de Conclave. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = 'http://localhost:8080';
  private static readonly tokenStorageKey = 'conclave_token';

  constructor(private http: HttpClient) {}

  /** Inicia sesión y devuelve los datos de autenticación. */
  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, { username, password });
  }

  /** Registra una cuenta y devuelve el mensaje del backend. */
  register(username: string, email: string, password: string): Observable<string> {
    return this.http.post(`${this.baseUrl}/auth/register`, { username, email, password }, { responseType: 'text' });
  }

  /** Obtiene el perfil del usuario autenticado. */
  profile(): Observable<unknown> {
    return this.http.get<unknown>(`${this.baseUrl}/user/me`, { headers: ApiService.authHeaders() });
  }

  /** Envía un prompt al proveedor de generación seleccionado. */
  generate(
    promptText: string,
    outputType: string,
    format?: string,
    imageProvider?: string,
    videoProvider?: string,
    videoModel?: string,
  ): Observable<GenerationResult[]> {
    const payload = {
      promptText,
      outputType,
      format: format ?? null,
      imageProvider: imageProvider ?? null,
      videoProvider: videoProvider ?? null,
      videoModel: videoModel ?? null,
    };
    return this.http.post<GenerationResult[]>(`${this.baseUrl}/prompt/generar`, payload, {
      headers: ApiService.authHeaders(),
    });
  }

  /** Construye los encabezados de autenticación si existe una sesión. */
  private static authHeaders(): HttpHeaders {
    const token = localStorage.getItem(ApiService.tokenStorageKey);
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
