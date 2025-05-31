import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvalSurveyComponent } from './eval-survey.component';

describe('EvalSurveyComponent', () => {
  let component: EvalSurveyComponent;
  let fixture: ComponentFixture<EvalSurveyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvalSurveyComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvalSurveyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
