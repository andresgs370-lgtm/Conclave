import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { App } from './app';
import { Navbarcomponent } from './component/navbarcomponent/navbarcomponent';
import { Paginaprincipalcomponent } from './component/paginaprincipalcomponent/paginaprincipalcomponent';
import { Footercomponent } from './component/footercomponent/footercomponent';
import { routes } from './app.routes';
import { Modeloscomponent } from './component/modeloscomponent/modeloscomponent';
import { Logincomponent } from './component/logincomponent/logincomponent';
import { Registrocomponent } from './component/registrocomponent/registrocomponent';
import { Videocomponent } from './component/videocomponent/videocomponent';
import { Imagencomponent } from './component/imagencomponent/imagencomponent';
import { Sonidocomponent } from './component/sonidocomponent/sonidocomponent';
import { Perfilcomponent } from './component/perfilcomponent/perfilcomponent';
import { Administrarcomponent } from './component/administrarcomponent/administrarcomponent';
/**
 * Módulo raíz de la aplicación Conclave.
 */
@NgModule({
  declarations: [
    App,
    Navbarcomponent,
    Paginaprincipalcomponent,
    Footercomponent,
    Modeloscomponent,
    Logincomponent,
    Registrocomponent,
    Videocomponent,
    Imagencomponent,
    Sonidocomponent,
    Perfilcomponent,
    Administrarcomponent,
  ],
  imports: [BrowserModule, HttpClientModule, FormsModule, RouterModule.forRoot(routes)],
  bootstrap: [App],
})
export class AppModule {} // skipcq: JS-0327
