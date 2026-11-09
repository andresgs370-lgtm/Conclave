import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';

import { App } from './app';
import { Navbarcomponent } from './component/navbarcomponent/navbarcomponent';
import { Paginaprincipalcomponent } from './component/paginaprincipalcomponent/paginaprincipalcomponent';
import { Footercomponent } from './component/footercomponent/footercomponent';
import { routes } from './app.routes';

@NgModule({
  declarations: [App, Navbarcomponent, Paginaprincipalcomponent, Footercomponent],
  imports: [BrowserModule, RouterModule.forRoot(routes)],
  bootstrap: [App],
})
export class AppModule {}
