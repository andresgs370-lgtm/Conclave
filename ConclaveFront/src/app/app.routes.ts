import { Routes } from '@angular/router';
import { Paginaprincipalcomponent } from './component/paginaprincipalcomponent/paginaprincipalcomponent';
import { Modeloscomponent } from './component/modeloscomponent/modeloscomponent';
import {Registrocomponent} from './component/registrocomponent/registrocomponent';
import { Logincomponent } from './component/logincomponent/logincomponent';
import { Videocomponent } from './component/videocomponent/videocomponent';
import { Imagencomponent } from './component/imagencomponent/imagencomponent';
import { Sonidocomponent } from './component/sonidocomponent/sonidocomponent';
import { Perfilcomponent } from './component/perfilcomponent/perfilcomponent';
import { Administrarcomponent} from './component/administrarcomponent/administrarcomponent';
import { Verificarcomponent } from './component/verificarcomponent/verificarcomponent';
import { Recuperarcomponent } from './component/recuperarcomponent/recuperarcomponent';

export const routes: Routes = [
  { path: '', component: Paginaprincipalcomponent },
  { path: 'modelo', component: Modeloscomponent },
  {path : 'login', component: Logincomponent },
  {path : 'registro', component:Registrocomponent  },
  {path : 'verificar', component: Verificarcomponent },
  {path : 'recuperar', component: Recuperarcomponent },
  {path : 'video', component:Videocomponent},
  {path : 'imagen', component:Imagencomponent},
  {path : 'sonido', component:Sonidocomponent},
  {path : 'perfil', component:Perfilcomponent},
  {path : 'administrar', component:Administrarcomponent},
];
