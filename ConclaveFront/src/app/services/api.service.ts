import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/login`, { username, password });
  }

  register(username: string, email: string, password: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/register`, { username, email, password }, { responseType: 'text' });
  }

  profile(): Observable<any> {
    const token = localStorage.getItem('conclave_token');
    const headers = token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : undefined;
    return this.http.get(`${this.baseUrl}/user/me`, { headers });
  }

  generate(promptText: string, outputType: string, format?: string): Observable<any> {
    const token = localStorage.getItem('conclave_token');
    const headers = token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : undefined;
    return this.http.post(`${this.baseUrl}/prompt/generar`, { promptText, outputType, format: format || null }, { headers });
  }
}
