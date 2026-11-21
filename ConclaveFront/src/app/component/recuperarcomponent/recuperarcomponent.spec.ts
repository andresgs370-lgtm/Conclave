import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter, RouterModule } from '@angular/router';
import { Recuperarcomponent } from './recuperarcomponent';
import '../../app.module';

/** Pruebas básicas de creación del componente Recuperarcomponent. */
describe('Recuperarcomponent', () => {
  let component: Recuperarcomponent;
  let fixture: ComponentFixture<Recuperarcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Recuperarcomponent],
      imports: [FormsModule, RouterModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: '**', children: [] }])],
    }).compileComponents();

    fixture = TestBed.createComponent(Recuperarcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
