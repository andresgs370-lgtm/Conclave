import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';

import { App } from './app';
import { Navbarcomponent } from './component/navbarcomponent/navbarcomponent';
import { Paginaprincipalcomponent } from './component/paginaprincipalcomponent/paginaprincipalcomponent';
import { Footercomponent } from './component/footercomponent/footercomponent';
import { routes } from './app.routes';
import { Modeloscomponent } from './component/modeloscomponent/modeloscomponent';

@NgModule({
  declarations: [App, Navbarcomponent, Paginaprincipalcomponent, Footercomponent, Modeloscomponent],
  imports: [BrowserModule, RouterModule.forRoot(routes)],
  bootstrap: [App],
})
export class AppModule {}
