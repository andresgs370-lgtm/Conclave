import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter, RouterModule } from '@angular/router';
import { Logincomponent } from './logincomponent';
import '../../app.module';

/** Pruebas básicas de creación del componente Logincomponent. */
describe('Logincomponent', () => {
  let component: Logincomponent;
  let fixture: ComponentFixture<Logincomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Logincomponent],
      imports: [FormsModule, RouterModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: '**', children: [] }])],
    }).compileComponents();

    fixture = TestBed.createComponent(Logincomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
