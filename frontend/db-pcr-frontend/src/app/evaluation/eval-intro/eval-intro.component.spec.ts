import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvalIntroComponent } from './eval-intro.component';

describe('EvalIntroComponent', () => {
  let component: EvalIntroComponent;
  let fixture: ComponentFixture<EvalIntroComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvalIntroComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvalIntroComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
