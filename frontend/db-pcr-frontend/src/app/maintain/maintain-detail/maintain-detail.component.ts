import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-maintain-detail',
  imports: [FormsModule],
  templateUrl: './maintain-detail.component.html',
  styleUrl: './maintain-detail.component.css',
})
export class MaintainDetailComponent implements OnInit {
  projectId: string = '';
  reviewerNum: number = 2;

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    this.projectId = this.route.snapshot.params['projectId'];
  }

  assign() {}
}
