import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthorReviewDetailComponent } from './author-review-detail.component';

describe('AuthorReviewDetailComponent', () => {
  let component: AuthorReviewDetailComponent;
  let fixture: ComponentFixture<AuthorReviewDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthorReviewDetailComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AuthorReviewDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
