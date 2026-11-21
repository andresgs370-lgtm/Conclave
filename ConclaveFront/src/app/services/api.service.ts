import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, timeout } from 'rxjs';

export interface AuthResponse {
  token: string;
  username: string;
  role?: string;
}

export interface PromptHistoryEntry {
  id: number;
  username?: string;
  promptText?: string;
  outputType?: string;
  format?: string;
  model?: string;
  status?: string;
  createdAt?: string;
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

  /**
   * Verifica la cuenta con el código de 6 dígitos enviado al correo al registrarse.
   *
   * @param email correo con el que se registró el usuario
   * @param codigo código recibido por correo
   * @returns mensaje del backend
   */
  verificarRegistro(email: string, codigo: string): Observable<string> {
    return this.http.post(`${this.baseUrl}/auth/verificar-registro`, { email, codigo }, { responseType: 'text' });
  }

  /**
   * Reenvía un código nuevo de verificación de cuenta al correo.
   *
   * @param email correo con el que se registró el usuario
   * @returns mensaje del backend
   */
  reenviarCodigo(email: string): Observable<string> {
    return this.http.post(`${this.baseUrl}/auth/reenviar-codigo`, { email }, { responseType: 'text' });
  }

  /**
   * Solicita el envío de un código para restablecer la contraseña.
   *
   * @param email correo de la cuenta
   * @returns mensaje del backend
   */
  solicitarReset(email: string): Observable<string> {
    return this.http.post(`${this.baseUrl}/auth/solicitar-reset`, { email }, { responseType: 'text' });
  }

  /**
   * Confirma el restablecimiento de contraseña con el código recibido por correo.
   *
   * @param email correo de la cuenta
   * @param codigo código recibido por correo
   * @param nuevaPassword nueva contraseña
   * @returns mensaje del backend
   */
  confirmarReset(email: string, codigo: string, nuevaPassword: string): Observable<string> {
    return this.http.post(`${this.baseUrl}/auth/confirmar-reset`, { email, codigo, nuevaPassword }, { responseType: 'text' });
  }

  /** Obtiene el perfil del usuario autenticado. */
  profile(): Observable<unknown> {
    return this.http.get<unknown>(`${this.baseUrl}/user/me`, { headers: ApiService.authHeaders() }).pipe(timeout({ first: 12000 }));
  }

  /** Obtiene el historial de prompts del usuario autenticado. */
  promptHistory(): Observable<PromptHistoryEntry[]> {
    return this.http.get<PromptHistoryEntry[]>(`${this.baseUrl}/history/me`, {
      headers: ApiService.authHeaders(),
    }).pipe(timeout({ first: 10000 }));
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
