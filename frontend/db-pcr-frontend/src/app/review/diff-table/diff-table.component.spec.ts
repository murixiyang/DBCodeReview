import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DiffTableComponent } from './diff-table.component';

describe('DiffTableComponent', () => {
  let component: DiffTableComponent;
  let fixture: ComponentFixture<DiffTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DiffTableComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DiffTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
