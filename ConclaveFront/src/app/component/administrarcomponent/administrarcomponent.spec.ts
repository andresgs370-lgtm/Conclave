import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Administrarcomponent } from './administrarcomponent';

describe('Administrarcomponent', () => {
  let component: Administrarcomponent;
  let fixture: ComponentFixture<Administrarcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Administrarcomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Administrarcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
