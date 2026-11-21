import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Recuperarcomponent } from './recuperarcomponent';

describe('Recuperarcomponent', () => {
  let component: Recuperarcomponent;
  let fixture: ComponentFixture<Recuperarcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Recuperarcomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Recuperarcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
