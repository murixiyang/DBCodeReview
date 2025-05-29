import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvalAuthorComponent } from './eval-author.component';

describe('EvalAuthorComponent', () => {
  let component: EvalAuthorComponent;
  let fixture: ComponentFixture<EvalAuthorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvalAuthorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvalAuthorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
