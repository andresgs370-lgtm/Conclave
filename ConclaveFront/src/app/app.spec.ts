import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter, RouterModule } from '@angular/router';
import { App } from './app';
import { Navbarcomponent } from './component/navbarcomponent/navbarcomponent';
import { Footercomponent } from './component/footercomponent/footercomponent';
import { Toastcomponent } from './component/toastcomponent/toastcomponent';
import './app.module';

/** Pruebas del componente raíz de la aplicación. */
describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [App, Navbarcomponent, Footercomponent, Toastcomponent],
      imports: [FormsModule, RouterModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: '**', children: [] }])],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renderiza la barra de navegación, el contenido y el pie de página', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-navbarcomponent')).not.toBeNull();
    expect(compiled.querySelector('router-outlet')).not.toBeNull();
    expect(compiled.querySelector('app-footercomponent')).not.toBeNull();
  });
});
