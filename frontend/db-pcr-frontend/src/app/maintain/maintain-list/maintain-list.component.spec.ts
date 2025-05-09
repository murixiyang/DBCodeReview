import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintainListComponent } from './maintain-list.component';

describe('MaintainListComponent', () => {
  let component: MaintainListComponent;
  let fixture: ComponentFixture<MaintainListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MaintainListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MaintainListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
