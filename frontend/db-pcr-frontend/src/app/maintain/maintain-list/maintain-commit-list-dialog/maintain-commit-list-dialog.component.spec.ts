import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintainCommitListDialogComponent } from './maintain-commit-list-dialog.component';

describe('MaintainCommitListDialogComponent', () => {
  let component: MaintainCommitListDialogComponent;
  let fixture: ComponentFixture<MaintainCommitListDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MaintainCommitListDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MaintainCommitListDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
