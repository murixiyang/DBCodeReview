import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvalTableComponent } from './eval-table.component';

describe('EvalTableComponent', () => {
  let component: EvalTableComponent;
  let fixture: ComponentFixture<EvalTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvalTableComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvalTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
