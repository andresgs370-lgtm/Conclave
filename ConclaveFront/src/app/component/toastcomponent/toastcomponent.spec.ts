import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, RouterModule } from '@angular/router';
import { Toastcomponent } from './toastcomponent';
import '../../app.module';

/** Pruebas del componente que muestra los avisos de generación terminada. */
describe('Toastcomponent', () => {
  let component: Toastcomponent;
  let fixture: ComponentFixture<Toastcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Toastcomponent],
      imports: [RouterModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Toastcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  /** Espera a que Angular repinte la vista después de cambiar el signal de avisos. */
  async function refrescar(): Promise<void> {
    await fixture.whenStable();
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('no muestra avisos cuando no hay toasts', () => {
    const items = (fixture.nativeElement as HTMLElement).querySelectorAll('.toast-item');
    expect(items.length).toBe(0);
  });

  it('muestra un aviso por cada toast', async () => {
    component.generacion.toasts.set([
      { id: 1, mensaje: 'Tu imagen ya está lista.', ruta: '/imagen', exito: true },
      { id: 2, mensaje: 'No se pudo generar tu video.', ruta: '/video', exito: false },
    ]);
    await refrescar();

    const elemento = fixture.nativeElement as HTMLElement;
    const items = elemento.querySelectorAll('.toast-item');
    expect(items.length).toBe(2);
    expect(items[0].classList).toContain('toast-ok');
    expect(items[1].classList).toContain('toast-fail');
    expect(elemento.textContent).toContain('Tu imagen ya está lista.');
  });

  it('el botón de cerrar quita el aviso', async () => {
    component.generacion.toasts.set([{ id: 1, mensaje: 'Tu audio ya está lista.', ruta: '/sonido', exito: true }]);
    await refrescar();

    (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('.toast-close')?.click();
    expect(component.generacion.toasts().length).toBe(0);
  });
});
