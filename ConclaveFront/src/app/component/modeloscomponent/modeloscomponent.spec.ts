import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Modeloscomponent } from './modeloscomponent';

describe('Modeloscomponent', () => {
  let component: Modeloscomponent;
  let fixture: ComponentFixture<Modeloscomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Modeloscomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Modeloscomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
