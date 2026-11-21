import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter, RouterModule } from '@angular/router';
import { Verificarcomponent } from './verificarcomponent';
import '../../app.module';

/** Pruebas básicas de creación del componente Verificarcomponent. */
describe('Verificarcomponent', () => {
  let component: Verificarcomponent;
  let fixture: ComponentFixture<Verificarcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Verificarcomponent],
      imports: [FormsModule, RouterModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: '**', children: [] }])],
    }).compileComponents();

    fixture = TestBed.createComponent(Verificarcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
