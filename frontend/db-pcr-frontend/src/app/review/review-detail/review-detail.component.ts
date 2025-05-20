import { Component, ElementRef, ViewChild } from '@angular/core';
import { Diff2HtmlUIConfig } from 'diff2html/lib/ui/js/diff2html-ui-base.js';
import { Diff2HtmlUI } from 'diff2html/lib/ui/js/diff2html-ui-slim.js';
import { ActivatedRoute } from '@angular/router';
import { ReviewService } from '../../http/review.service.js';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input.js';
import { NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommentRange } from '../../interface/gerrit/comment-range.js';

interface DraftComment {
  // editor position:
  x: number;
  y: number;
  // gerrit API fields:
  path: string;
  side: 'PARENT' | 'REVISION';
  line: number;
  range?: CommentRange;
  message: string;
}

@Component({
  selector: 'app-review-detail',
  imports: [NgFor, FormsModule],
  templateUrl: './review-detail.component.html',
  styleUrl: './review-detail.component.css',
})
export class ReviewDetailComponent {
  gerritChangeId!: string;

  rawDiff: string = '';

  draftComments: DraftComment[] = [];

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
    const container = this.diffContainer.nativeElement as HTMLElement;
    container.innerHTML = '';
    const ui = new Diff2HtmlUI(container, this.rawDiff, this.config);
    ui.draw();
    ui.highlightCode();

    // AFTER rendering the diff, hook up line‐number clicks
    // each line in side-by-side has d2h-code-linenumber class
    const lineEls = container.querySelectorAll<HTMLElement>(
      '.d2h-code-side-linenumber'
    );
    lineEls.forEach((el) => {
      el.addEventListener('click', (evt) => {
        const target = evt.currentTarget as HTMLElement;

        console.log('cell classes:', target.className);
        console.log('dataset:', target.dataset);

        // extract path, line, side from the DOM
        const text = target.textContent?.trim() ?? '';
        const line = parseInt(text, 10);
        if (isNaN(line)) {
          console.error('Invalid line number:', text);
          return;
        }
        console.log('line', line);

        const cls = target.classList;
        let side: 'PARENT' | 'REVISION';
        if (
          cls.contains('d2h-ins') ||
          cls.contains('d2h-code-side-linenumber-new')
        ) {
          side = 'REVISION';
        } else {
          side = 'PARENT';
        }
        console.log('side', side);

        // path is in the file header: climb up to .d2h-file-name
        const fileHeader = target
          .closest('.d2h-file-wrapper')
          ?.querySelector('.d2h-file-header .d2h-file-name');
        const path = fileHeader?.textContent?.trim() || '';
        console.log('path', path);

        // get click position to place textarea
        const rect = container.getBoundingClientRect();
        const x = evt.clientX - rect.left;
        const y = evt.clientY - rect.top;

        const draft: DraftComment = {
          x: x,
          y: y,
          path: path,
          line: line,
          side: side,
          range: {
            startLine: line,
            startCharacter: 1,
            endLine: line,
            endCharacter: 1,
          },
          message: '',
        };

        this.draftComments.push(draft);
      });
    });
  }

  saveComment(draft: DraftComment) {
    const payload: GerritCommentInput = {
      path: draft.path,
      side: draft.side,
      line: draft.line,
      message: draft.message,
    };

    this.reviewSvc
      .postDraftComment(this.gerritChangeId, draft)
      .subscribe((commitInfo) => {
        // remove editor on success
        console.log('Pushed commentInput', draft);
        console.log('commitInfo', commitInfo);
        this.draftComments = this.draftComments.filter((dc) => dc !== draft);
      });
  }

  cancelComment(draft: DraftComment) {
    this.draftComments = this.draftComments.filter((dc) => dc !== draft);
  }

  createDraftComment() {
    const commentInput: GerritCommentInput = {
      path: 'README.md',
      line: 1,
      side: 'REVISION',
      range: {
        startLine: 1,
        startCharacter: 1,
        endLine: 1,
        endCharacter: 1,
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
