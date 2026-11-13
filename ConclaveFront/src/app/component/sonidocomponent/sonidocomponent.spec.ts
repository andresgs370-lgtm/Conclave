import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Sonidocomponent } from './sonidocomponent';

describe('Sonidocomponent', () => {
  let component: Sonidocomponent;
  let fixture: ComponentFixture<Sonidocomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Sonidocomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Sonidocomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
