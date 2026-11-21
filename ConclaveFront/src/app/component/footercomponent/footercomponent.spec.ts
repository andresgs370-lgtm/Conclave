import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter, RouterModule } from '@angular/router';
import { Footercomponent } from './footercomponent';
import '../../app.module';

/** Pruebas básicas de creación del componente Footercomponent. */
describe('Footercomponent', () => {
  let component: Footercomponent;
  let fixture: ComponentFixture<Footercomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Footercomponent],
      imports: [FormsModule, RouterModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: '**', children: [] }])],
    }).compileComponents();

    fixture = TestBed.createComponent(Footercomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
