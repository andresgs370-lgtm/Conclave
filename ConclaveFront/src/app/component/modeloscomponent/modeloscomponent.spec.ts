import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter, RouterModule } from '@angular/router';
import { Modeloscomponent } from './modeloscomponent';
import '../../app.module';

/** Pruebas básicas de creación del componente Modeloscomponent. */
describe('Modeloscomponent', () => {
  let component: Modeloscomponent;
  let fixture: ComponentFixture<Modeloscomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Modeloscomponent],
      imports: [FormsModule, RouterModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: '**', children: [] }])],
    }).compileComponents();

    fixture = TestBed.createComponent(Modeloscomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
