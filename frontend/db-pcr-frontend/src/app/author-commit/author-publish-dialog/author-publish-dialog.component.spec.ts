import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthorPublishDialogComponent } from './author-publish-dialog.component';

describe('AuthorPublishDialogComponent', () => {
  let component: AuthorPublishDialogComponent;
  let fixture: ComponentFixture<AuthorPublishDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthorPublishDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AuthorPublishDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
