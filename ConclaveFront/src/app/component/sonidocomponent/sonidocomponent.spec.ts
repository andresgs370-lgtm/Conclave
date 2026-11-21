import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter, RouterModule } from '@angular/router';
import { Sonidocomponent } from './sonidocomponent';
import '../../app.module';

/** Pruebas básicas de creación del componente Sonidocomponent. */
describe('Sonidocomponent', () => {
  let component: Sonidocomponent;
  let fixture: ComponentFixture<Sonidocomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Sonidocomponent],
      imports: [FormsModule, RouterModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: '**', children: [] }])],
    }).compileComponents();

    fixture = TestBed.createComponent(Sonidocomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
