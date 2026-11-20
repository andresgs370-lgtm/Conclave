import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

/** Usuario tal como lo devuelve el backend (UserDTO). */
interface User {
  id: number;
  username: string;
  email: string;
  role: string;
  enabled?: boolean;
}

/** Registro del historial de actividad de un usuario. */
interface HistItem {
  timestamp: string;
  username: string;
  action: string;
  detail: string;
}

/**
 * Panel de administración de Conclave. Solo debe ser accesible para usuarios con rol ADMIN.
 * Permite revisar y modificar usuarios, gestionar la API key y consultar el historial.
 */
@Component({
  selector: 'app-administrarcomponent',
  standalone: false,
  templateUrl: './administrarcomponent.html',
  styleUrls: ['./administrarcomponent.css']
})
export class Administrarcomponent implements OnInit {

  /** URL base del backend. */
  private api = 'http://localhost:8080';

  /** Encabezados de autenticación con el token de la sesión actual. */
  private get headers(): HttpHeaders {
    const token = this.auth.token;
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }

  /** Pestaña visible actualmente. */
  tab: 'usuarios' | 'apikey' | 'historial' = 'usuarios';

  /** Texto del aviso emergente. Vacío cuando no hay aviso. */
  mensaje = '';

  /** Lista de usuarios cargada desde el backend. */
  usuarios: User[] = [];

  /** Texto del buscador de usuarios. */
  filtro = '';

  /** Usuario en edición. Es `null` cuando el modal está cerrado. */
  editando: User | null = null;

  /** Datos del formulario de edición. */
  form = { username: '', email: '', role: 'USER' };

  /** Mensaje de error del modal de edición. */
  editError = '';

  /** Valor actual de la API key. */
  apiKey = '';

  /** Indica si la API key se muestra completa o enmascarada. */
  keyVisible = false;

  /** Mensaje de error de la pestaña API key. */
  keyError = '';

  /** Indica si el modal para cambiar la API key está abierto. */
  cambiandoKey = false;

  /** Indica si el modal de confirmación para generar una nueva API key está abierto. */
  confirmandoRegenerar = false;

  /** Valor escrito en el modal para la nueva API key. */
  nuevaKey = '';

  /** Registros de historial mostrados en la tabla. */
  historial: HistItem[] = [];

  /** ID del usuario por el que se filtra el historial. Con `null` se muestran todos. */
  histUsuarioId: number | null = null;

  /** Mensaje mostrado cuando no hay registros o ocurre un error. */
  histMensaje = 'Sin actividad para mostrar.';

  /** Inyecta el cliente HTTP y el servicio de sesión. */
  constructor(private http: HttpClient, private auth: AuthService) {}

  /** Carga la lista de usuarios al iniciar el componente. */
  ngOnInit(): void { this.cargarUsuarios(); }

  /** Muestra un aviso emergente durante unos segundos. */
  private avisar(m: string): void {
    this.mensaje = m;
    setTimeout(() => (this.mensaje = ''), 2400);
  }

  /** Cambia la pestaña visible y carga los datos que necesita. */
  cambiarTab(t: 'usuarios' | 'apikey' | 'historial'): void {
    this.tab = t;
    if (t === 'apikey') this.cargarKey();
    if (t === 'historial') this.cargarHistorial();
  }

  /** Usuarios cuyo nombre o correo coincide con el texto del buscador. */
  get usuariosFiltrados(): User[] {
    const texto = this.filtro.trim().toLowerCase();
    return this.usuarios.filter(usuario =>
      `${usuario.username} ${usuario.email}`.toLowerCase().includes(texto)
    );
  }

  /** Carga todos los usuarios con `GET /user/getall`. El código 204 sin cuerpo se trata como lista vacía. */
  cargarUsuarios(): void {
    this.http.get<User[]>(`${this.api}/user/getall`, { observe: 'response', headers: this.headers }).subscribe({
      next: r => (this.usuarios = r.body ?? []),
      error: e => this.avisar(`No se pudieron cargar los usuarios (error ${e.status})`)
    });
  }

  /** Abre el modal de edición con los datos del usuario seleccionado. */
  abrirEditar(u: User): void {
    this.editando = u;
    this.form = { username: u.username, email: u.email, role: u.role };
    this.editError = '';
  }

  /** Cierra el modal de edición sin guardar cambios. */
  cerrarEditar(): void { this.editando = null; }

  /**
   * Guarda los cambios del usuario con `PUT /user/updatejson?id=`.
   * El backend responde 226 cuando el nuevo username ya está en uso; como es un código 2xx,
   * se revisa el status de forma manual.
   */
  guardarUsuario(): void {
    if (!this.editando) return;
    const payload = { ...this.editando, ...this.form };
    this.http.put(`${this.api}/user/updatejson`, payload, {
      params: { id: this.editando.id },
      observe: 'response',
      responseType: 'text',
      headers: this.headers
    }).subscribe({
      next: r => {
        if (r.status === 226) { this.editError = r.body || 'El nuevo nombre de usuario ya está en uso'; return; }
        this.editando = null;
        this.avisar('Cambios guardados');
        this.cargarUsuarios();
      },
      error: e => (this.editError = (typeof e.error === 'string' && e.error) || 'No se pudo guardar')
    });
  }

  /** Suspende o activa la cuenta del usuario con `PUT /user/setenabled/{id}?enabled=`. */
  cambiarEstado(u: User): void {
    const enabled = u.enabled === false;
    this.http.put(`${this.api}/user/setenabled/${u.id}`, null, {
      params: { enabled }, responseType: 'text', headers: this.headers
    }).subscribe({
      next: () => { this.avisar('Estado actualizado'); this.cargarUsuarios(); },
      error: () => this.avisar('No se pudo cambiar el estado')
    });
  }

  /** Abre la pestaña de historial filtrada por el usuario indicado. */
  verHistorial(u: User): void {
    this.histUsuarioId = u.id;
    this.cambiarTab('historial');
  }

  /** API key con los primeros 6 y los últimos 4 caracteres visibles. */
  get keyEnmascarada(): string {
    const k = this.apiKey;
    return k ? k.slice(0, 6) + '•'.repeat(Math.max(8, k.length - 10)) + k.slice(-4) : '';
  }

  /** Consulta la API key actual con `GET /apikey`. Espera la forma `{ apiKey: string }`. */
  cargarKey(): void {
    this.keyError = '';
    this.http.get<{ apiKey: string }>(`${this.api}/apikey`, { headers: this.headers }).subscribe({
      next: r => (this.apiKey = r?.apiKey ?? ''),
      error: e => (this.keyError = e.status === 404 ? 'El endpoint GET /apikey aún no existe en el backend.' : 'No se pudo cargar la API key.')
    });
  }

  /** Abre el modal para escribir una nueva API key. */
  abrirCambiarKey(): void { this.nuevaKey = ''; this.cambiandoKey = true; }

  /** Guarda la API key escrita en el modal con `PUT /apikey`. */
  guardarKey(): void {
    this.http.put(`${this.api}/apikey`, { apiKey: this.nuevaKey.trim() }, { headers: this.headers }).subscribe({
      next: () => { this.cambiandoKey = false; this.avisar('API key actualizada'); this.cargarKey(); },
      error: () => (this.keyError = 'No se pudo guardar la API key.')
    });
  }

  /** Abre el modal de confirmación antes de generar una nueva API key. */
  regenerarKey(): void { this.confirmandoRegenerar = true; }

  /** Genera una API key nueva con `POST /apikey/regenerate` tras la confirmación del administrador. */
  confirmarRegenerar(): void {
    this.confirmandoRegenerar = false;
    this.http.post(`${this.api}/apikey/regenerate`, null, { headers: this.headers }).subscribe({
      next: () => { this.keyVisible = true; this.avisar('Nueva API key generada'); this.cargarKey(); },
      error: () => (this.keyError = 'No se pudo generar la API key.')
    });
  }

  /** Carga el historial con `GET /history/user/{id}` si hay usuario seleccionado, o con `GET /history` si no. */
  cargarHistorial(): void {
    const url = this.histUsuarioId != null ? `${this.api}/history/user/${this.histUsuarioId}` : `${this.api}/history`;
    this.http.get<HistItem[]>(url, { headers: this.headers }).subscribe({
      next: r => { this.historial = r ?? []; this.histMensaje = 'Sin actividad para mostrar.'; },
      error: e => {
        this.historial = [];
        this.histMensaje = e.status === 404 ? 'El endpoint de historial aún no existe en el backend.' : 'No se pudo cargar el historial.';
      }
    });
  }
}
