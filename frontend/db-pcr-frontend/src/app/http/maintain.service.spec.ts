import { TestBed } from '@angular/core/testing';

import { MaintainService } from './maintain.service';

describe('MaintainService', () => {
  let service: MaintainService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MaintainService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
