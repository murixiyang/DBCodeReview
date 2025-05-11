import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommitDetailComponent } from './commit-details.component';

describe('CommitDetailComponent', () => {
  let component: CommitDetailComponent;
  let fixture: ComponentFixture<CommitDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommitDetailComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CommitDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
