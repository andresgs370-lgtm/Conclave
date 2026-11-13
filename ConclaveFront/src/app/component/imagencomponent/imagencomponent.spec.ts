import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Imagencomponent } from './imagencomponent';

describe('Imagencomponent', () => {
  let component: Imagencomponent;
  let fixture: ComponentFixture<Imagencomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Imagencomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Imagencomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
