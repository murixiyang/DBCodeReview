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

    this.reviewSvc.getExistedComments(this.gerritChangeId).subscribe((c) => {
      this.existedComments = c;
      console.log('existed comments: ', c);
    });

    this.reviewSvc.getDraftComments(this.gerritChangeId).subscribe((d) => {
      this.draftComments = d;
      console.log('draft comments: ', d);
    });

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

  filteredExistedComments(filename: string): GerritCommentInfo[] {
    return this.existedComments.filter((c) => c.path === filename);
  }

  filteredDraftComments(filename: string): GerritCommentInput[] {
    return this.draftComments.filter((c) => c.path === filename);
  }

  onLineClick(fileName: string, side: 'OLD' | 'NEW', evt: any) {
    console.log('line clicked: ', evt);

    const { index, line, lineNumberInNewText, lineNumberInOldText, type } = evt;

    this.draftComments.push({
      path: fileName,
      line: side === 'NEW' ? lineNumberInNewText : lineNumberInOldText,
      side: side,
      message: '',
    });

    console.log('draft comments: ', this.draftComments);
  }
}
