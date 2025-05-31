import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvalReviewComponent } from './eval-review.component';

describe('EvalReviewComponent', () => {
  let component: EvalReviewComponent;
  let fixture: ComponentFixture<EvalReviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvalReviewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvalReviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
