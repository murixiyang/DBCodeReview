import { Component, ElementRef, ViewChild } from '@angular/core';
import { Diff2HtmlUIConfig } from 'diff2html/lib/ui/js/diff2html-ui-base.js';
import { Diff2HtmlUI } from 'diff2html/lib/ui/js/diff2html-ui-slim.js';
import { ActivatedRoute } from '@angular/router';
import { ReviewService } from '../../http/review.service.js';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input.js';

@Component({
  selector: 'app-review-detail',
  imports: [],
  templateUrl: './review-detail.component.html',
  styleUrl: './review-detail.component.css',
})
export class ReviewDetailComponent {
  gerritChangeId!: string;

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
    this.gerritChangeId = this.route.snapshot.paramMap.get('gerritChangeId')!;
    this.loadDiffs();

    this.reviewSvc
      .getExistedComments(this.gerritChangeId)
      .subscribe((comments) => {
        console.log('comments', comments);
      });
  }

  private loadDiffs() {
    this.reviewSvc.getChangeDiffs(this.gerritChangeId).subscribe((diff) => {
      this.rawDiff = diff;
      // trigger a re-render
      setTimeout(() => this.render(), 0);

      console.log('rawDiff', this.rawDiff);
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
    const ui = new Diff2HtmlUI(
      this.diffContainer.nativeElement,
      this.rawDiff,
      this.config
    );
    ui.draw();
    ui.highlightCode();
  }

  createDraftComment() {
    const commentInput: GerritCommentInput = {
      path: 'README.md',
      line: 1,
      side: 'REVISION',
      range: {
        start_line: 1,
        start_character: 1,
        end_line: 1,
        end_character: 1,
      },
      message: 'This is a test comment',
    };

    this.reviewSvc
      .postDraftComment(this.gerritChangeId, commentInput)
      .subscribe((comment) => {
        console.log('comment', comment);
      });
  }
}
