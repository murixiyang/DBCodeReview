import {
  Component,
  ElementRef,
  QueryList,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { ChangeDiff } from '../../interface/gerrit/change-diff.ts';
import {
  Diff2HtmlUI,
  Diff2HtmlUIConfig,
} from 'diff2html/lib/ui/js/diff2html-ui-base.js';
import { ActivatedRoute } from '@angular/router';
import { ReviewService } from '../../http/review.service.js';
import { NgFor, NgIf } from '@angular/common';

@Component({
  selector: 'app-review-detail',
  imports: [],
  templateUrl: './review-detail.component.html',
  styleUrl: './review-detail.component.css',
})
export class ReviewDetailComponent {
  assignmentUuid!: string;
  changeId!: string;

  rawDiff: string = '';
  private config: Diff2HtmlUIConfig = {
    drawFileList: false,
    outputFormat: 'side-by-side',
    matching: 'lines',
    highlight: true,
  };

  @ViewChild('diffContainer', { read: ElementRef })
  diffContainer!: ElementRef<HTMLDivElement>;

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
      .subscribe((diff) => {
        this.rawDiff = diff;
        console.log('rawDiff', diff);
        // trigger a re-render
        setTimeout(() => this.render(), 0);
      });
  }

  ngAfterViewInit() {
    this.render();
  }

  private render() {
    if (!this.rawDiff || !this.diffContainer) return;
    // clear any previous content
    this.diffContainer.nativeElement.innerHTML = '';
    // instantiate and draw only the diffs
    new Diff2HtmlUI(
      this.diffContainer.nativeElement,
      this.rawDiff,
      this.config
    ).draw();
  }
}
