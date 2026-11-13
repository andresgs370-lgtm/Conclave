import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
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
  ],
  imports: [BrowserModule, RouterModule.forRoot(routes)],
  bootstrap: [App],
})
export class AppModule {} // skipcq: JS-0327
