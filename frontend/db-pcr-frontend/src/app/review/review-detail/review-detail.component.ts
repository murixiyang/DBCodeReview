import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReviewService } from '../../http/review.service.js';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input.js';
import { FormsModule } from '@angular/forms';
import { GerritCommentInfo } from '../../interface/gerrit/gerrit-comment-info.js';
import { NgFor } from '@angular/common';
import { DiffTableComponent } from '../diff-table/diff-table.component.js';

@Component({
  standalone: true,
  selector: 'app-review-detail',
  imports: [FormsModule, NgFor, DiffTableComponent],
  templateUrl: './review-detail.component.html',
  styleUrl: './review-detail.component.css',
})
export class ReviewDetailComponent {
  gerritChangeId!: string;

  fileContents: Map<string, string[]> = new Map();

  existedComments: GerritCommentInfo[] = [];

  draftComments: GerritCommentInput[] = [];

  constructor(
    private route: ActivatedRoute,
    private reviewSvc: ReviewService
  ) {}

  ngOnInit() {
    this.gerritChangeId = this.route.snapshot.paramMap.get('gerritChangeId')!;

    this.reviewSvc
      .getChangedFileContents(this.gerritChangeId)
      .subscribe((f) => {
        console.log('changed file contents: ', f);

        this.fileContents = new Map(Object.entries(f));
      });
  }

  get fileKeys(): string[] {
    return Array.from(this.fileContents.keys());
  }


}
