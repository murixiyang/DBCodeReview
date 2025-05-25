import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthorDiffTableComponent } from './author-diff-table.component';

describe('AuthorDiffTableComponent', () => {
  let component: AuthorDiffTableComponent;
  let fixture: ComponentFixture<AuthorDiffTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthorDiffTableComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AuthorDiffTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
