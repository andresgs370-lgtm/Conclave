import { TestBed } from '@angular/core/testing';
import { Generacionservice } from './generacionservice';

describe('Generacionservice', () => {
  let service: Generacionservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Generacionservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
