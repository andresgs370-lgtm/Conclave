import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Verificarcomponent } from './verificarcomponent';

describe('Verificarcomponent', () => {
  let component: Verificarcomponent;
  let fixture: ComponentFixture<Verificarcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Verificarcomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Verificarcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
