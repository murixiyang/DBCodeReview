import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReviewCommitListComponent } from './review-commit-list.component';

describe('ReviewCommitListComponent', () => {
  let component: ReviewCommitListComponent;
  let fixture: ComponentFixture<ReviewCommitListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReviewCommitListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReviewCommitListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
