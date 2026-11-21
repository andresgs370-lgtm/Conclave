import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter, RouterModule } from '@angular/router';
import { Paginaprincipalcomponent } from './paginaprincipalcomponent';
import '../../app.module';

/** Pruebas básicas de creación del componente Paginaprincipalcomponent. */
describe('Paginaprincipalcomponent', () => {
  let component: Paginaprincipalcomponent;
  let fixture: ComponentFixture<Paginaprincipalcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Paginaprincipalcomponent],
      imports: [FormsModule, RouterModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: '**', children: [] }])],
    }).compileComponents();

    fixture = TestBed.createComponent(Paginaprincipalcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
