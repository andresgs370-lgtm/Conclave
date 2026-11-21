import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter, RouterModule } from '@angular/router';
import { Videocomponent } from './videocomponent';
import '../../app.module';

/** Pruebas básicas de creación del componente Videocomponent. */
describe('Videocomponent', () => {
  let component: Videocomponent;
  let fixture: ComponentFixture<Videocomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Videocomponent],
      imports: [FormsModule, RouterModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: '**', children: [] }])],
    }).compileComponents();

    fixture = TestBed.createComponent(Videocomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
