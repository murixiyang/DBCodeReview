import { Component, ElementRef, QueryList, ViewChildren } from '@angular/core';
import { ChangeDiff } from '../../interface/gerrit/change-diff.ts';
import { Diff2HtmlUI } from 'diff2html/lib/ui/js/diff2html-ui-base.js';
import { ActivatedRoute } from '@angular/router';
import { ReviewService } from '../../http/review.service.js';
import { NgFor, NgIf } from '@angular/common';

@Component({
  selector: 'app-review-detail',
  imports: [NgIf, NgFor],
  templateUrl: './review-detail.component.html',
  styleUrl: './review-detail.component.css',
})
export class ReviewDetailComponent {
  assignmentUuid!: string;
  changeId!: string;
  diffs: ChangeDiff[] = [];

  @ViewChildren('diffContainer', { read: ElementRef })
  diffContainers!: QueryList<ElementRef<HTMLDivElement>>;

  constructor(
    private route: ActivatedRoute,
    private reviewSvc: ReviewService
  ) {}

  ngOnInit() {
    this.assignmentUuid = this.route.snapshot.paramMap.get('assignmentUuid')!;
    this.changeId = this.route.snapshot.paramMap.get('changeId')!;
    this.loadDiffs();
  }

  private loadDiffs() {
    this.reviewSvc
      .getChangeDiffs(this.assignmentUuid, this.changeId)
      .subscribe((diffs) => {
        this.diffs = diffs;
        // trigger a re-render
        setTimeout(() => this.renderAll(), 0);
      });
  }

  ngAfterViewInit() {
    this.diffContainers.changes.subscribe(() => this.renderAll());
  }

  private renderAll() {
    this.diffContainers.forEach((elRef, idx) => {
      const diff = this.diffs[idx];
      if (!diff) return;

      console.log('Rendering diff:', diff);

      const ui = new Diff2HtmlUI(elRef.nativeElement, diff.diff, {
        outputFormat: 'side-by-side',
        drawFileList: false,
        matching: 'lines',
        highlight: true,
      });
      ui.draw();
    });
  }
}
