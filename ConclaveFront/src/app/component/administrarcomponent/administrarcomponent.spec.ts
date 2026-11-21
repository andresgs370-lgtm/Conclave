import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter, RouterModule } from '@angular/router';
import { Administrarcomponent } from './administrarcomponent';
import '../../app.module';

/** Pruebas básicas de creación del componente Administrarcomponent. */
describe('Administrarcomponent', () => {
  let component: Administrarcomponent;
  let fixture: ComponentFixture<Administrarcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Administrarcomponent],
      imports: [FormsModule, RouterModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: '**', children: [] }])],
    }).compileComponents();

    fixture = TestBed.createComponent(Administrarcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
