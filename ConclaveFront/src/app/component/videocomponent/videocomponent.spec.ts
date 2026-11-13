import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Videocomponent } from './videocomponent';

describe('Videocomponent', () => {
  let component: Videocomponent;
  let fixture: ComponentFixture<Videocomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Videocomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Videocomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
