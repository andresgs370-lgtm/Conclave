import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter, RouterModule } from '@angular/router';
import { Imagencomponent } from './imagencomponent';
import '../../app.module';

/** Pruebas básicas de creación del componente Imagencomponent. */
describe('Imagencomponent', () => {
  let component: Imagencomponent;
  let fixture: ComponentFixture<Imagencomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Imagencomponent],
      imports: [FormsModule, RouterModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: '**', children: [] }])],
    }).compileComponents();

    fixture = TestBed.createComponent(Imagencomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
