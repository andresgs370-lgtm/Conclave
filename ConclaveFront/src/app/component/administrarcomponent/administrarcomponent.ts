import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Authservice } from '../../services/authservice';
import { Router } from '@angular/router';

/** Usuario tal como lo devuelve el backend (UserDTO). */
interface User {
  id: number;
  username: string;
  email: string;
  role: string;
  enabled?: boolean;
}

/** Registro de historial tal como lo devuelve el backend (PromptHistoryDTO). */
interface PromptHistoryItem {
  createdAt: string;
  username: string | null;
  promptText: string;
  outputType: string | null;
  model: string | null;
  status: string | null;
}

/** Registro del historial de actividad de un usuario. */
interface HistItem {
  timestamp: string;
  username: string;
  action: string;
  detail: string;
}

/** API key registrada para un proveedor de IA. */
interface ProviderKey {
  provider: string;
  maskedKey: string;
  active: boolean;
  updatedAt: string | null;
  updatedBy: string | null;
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
  private api = 'https://gpcueb.org/conclave-0.0.1-SNAPSHOT';

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

  /** API keys registradas para los proveedores de IA. */
  providerKeys: ProviderKey[] = [];

  /** Proveedor seleccionado para editar su configuración. */
  providerEditando: ProviderKey | null = null;

  /** Datos del formulario de API key. */
  providerForm = { newKey: '', active: false };

  /** Mensaje de error de la gestión de proveedores. */
  keyError = '';

  /** Indica si se está guardando una API key. */
  guardandoKey = false;

  /** Registros de historial mostrados en la tabla. */
  historial: HistItem[] = [];

  /** ID del usuario por el que se filtra el historial. Con `null` se muestran todos. */
  histUsuarioId: number | null = null;

  /** Mensaje mostrado cuando no hay registros o ocurre un error. */
  histMensaje = 'Sin actividad para mostrar.';

  /** Inyecta el cliente HTTP y el servicio de sesión. */
  constructor(private http: HttpClient, private auth: Authservice, private router: Router) {}

  /** Carga los datos del panel cuando la sesión pertenece a un administrador. */
  ngOnInit(): void {
    if (!this.auth.isLoggedIn || !this.auth.isAdmin) {
      this.router.navigateByUrl('/login');
      return;
    }
    this.http.get<{ role: string }>(`${this.api}/user/me`, { headers: this.headers }).subscribe({
      next: user => {
        if ((user?.role ?? '').toUpperCase() !== 'ADMIN') {
          this.router.navigateByUrl('/');
          return;
        }
        this.cargarUsuarios();
      },
      error: () => this.router.navigateByUrl('/login')
    });
  }

  /** Muestra un aviso emergente durante unos segundos. */
  private avisar(m: string): void {
    this.mensaje = m;
    setTimeout(() => (this.mensaje = ''), 2400);
  }

  /** Cambia la pestaña visible y carga los datos que necesita. */
  cambiarTab(t: 'usuarios' | 'apikey' | 'historial'): void {
    this.tab = t;
    if (t === 'apikey') this.cargarKeys();
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

  /** Nombres visibles de los proveedores de IA. */
  private readonly nombresProveedores: Record<string, string> = {
    OPENROUTER: 'OpenRouter',
    GEMINI: 'Google Gemini',
    CLOUDFLARE: 'Cloudflare Workers AI (imágenes y voz)',
    POLLINATIONS: 'Pollinations.ai (key opcional)',
    TOGETHER_AI: 'Together AI',
    ELEVENLABS: 'ElevenLabs',
    HF_MINIMAX_H3: 'Hugging Face · MiniMax H3 FlashGen',
    HF_MINIMAX_TURBO: 'Hugging Face · MiniMax H3 Turbo',
    HF_OMNI_VIDEO: 'Hugging Face · Omni Video',
    HF_MODELSCOPE: 'Hugging Face · ModelScope Text-to-Video'
  };

  /** Nombre visible de cada proveedor de IA. */
  nombreProveedor(provider: string): string {
    return this.nombresProveedores[provider] ?? provider;
  }

  /** Consulta las API keys registradas con `GET /admin/apikey/getall`. */
  cargarKeys(): void {
    this.keyError = '';
    this.http.get<ProviderKey[]>(`${this.api}/admin/apikey/getall`, { headers: this.headers }).subscribe({
      next: r => (this.providerKeys = r ?? []),
      error: e => {
        this.providerKeys = [];
        this.keyError = e.status === 403 ? 'No tienes permisos para gestionar las API keys.' : 'No se pudieron cargar las API keys.';
      }
    });
  }

  /** Abre el formulario de edición del proveedor seleccionado. */
  abrirEditarKey(provider: ProviderKey): void {
    this.providerEditando = provider;
    this.providerForm = { newKey: '', active: provider.active };
    this.keyError = '';
  }

  /** Cierra el formulario de edición de API key. */
  cerrarEditarKey(): void {
    this.providerEditando = null;
    this.providerForm = { newKey: '', active: false };
    this.keyError = '';
  }

  /** Guarda la clave y el estado del proveedor con `PUT /admin/apikey/update/{provider}`. */
  guardarKey(): void {
    if (!this.providerEditando || this.guardandoKey) return;
    const provider = this.providerEditando.provider;
    const newKey = this.providerForm.newKey.trim();
    this.guardandoKey = true;
    this.keyError = '';
    this.http.put(`${this.api}/admin/apikey/update/${provider}`, {
      newKey: newKey || null,
      active: this.providerForm.active
    }, { responseType: 'text', headers: this.headers }).subscribe({
      next: () => {
        this.guardandoKey = false;
        this.cerrarEditarKey();
        this.avisar('Configuración actualizada');
        this.cargarKeys();
      },
      error: e => {
        this.guardandoKey = false;
        this.keyError = e.status === 404 ? 'No se encontró el proveedor seleccionado.' : 'No se pudo actualizar la configuración.';
      }
    });
  }

  /**
   * Convierte un registro del backend en la fila que muestra la tabla de historial.
   *
   * @param item registro de historial del backend
   * @returns fila de la tabla
   */
  private static toHistItem(item: PromptHistoryItem): HistItem {
    const partes = [item.outputType, item.model, item.status].filter(Boolean);
    return {
      timestamp: item.createdAt,
      username: item.username ?? '—',
      action: partes.join(' · '),
      detail: item.promptText,
    };
  }

  /** Carga el historial con `GET /history/user/{id}` si hay usuario seleccionado, o con `GET /history` si no. */
  cargarHistorial(): void {
    const url = this.histUsuarioId != null ? `${this.api}/history/user/${this.histUsuarioId}` : `${this.api}/history`;
    this.http.get<PromptHistoryItem[]>(url, { headers: this.headers }).subscribe({
      next: r => {
        this.historial = (r ?? []).map(Administrarcomponent.toHistItem);
        this.histMensaje = 'Sin actividad para mostrar.';
      },
      error: e => {
        this.historial = [];
        this.histMensaje = e.status === 404 ? 'El endpoint de historial aún no existe en el backend.' : 'No se pudo cargar el historial.';
      }
    });
  }
}
