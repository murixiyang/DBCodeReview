import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PublishDialogComponent } from './publish-dialog.component';

describe('PublishDialogComponent', () => {
  let component: PublishDialogComponent;
  let fixture: ComponentFixture<PublishDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PublishDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PublishDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
