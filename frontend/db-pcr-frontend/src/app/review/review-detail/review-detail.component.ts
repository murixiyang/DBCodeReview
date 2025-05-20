import { Component, ElementRef, ViewChild } from '@angular/core';
import { Diff2HtmlUIConfig } from 'diff2html/lib/ui/js/diff2html-ui-base.js';
import { Diff2HtmlUI } from 'diff2html/lib/ui/js/diff2html-ui-slim.js';
import { ActivatedRoute } from '@angular/router';
import { ReviewService } from '../../http/review.service.js';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input.js';
import { NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommentRange } from '../../interface/gerrit/comment-range.js';
import { GerritCommentInfo } from '../../interface/gerrit/gerrit-comment-info.js';

@Component({
  selector: 'app-review-detail',
  imports: [FormsModule],
  templateUrl: './review-detail.component.html',
  styleUrl: './review-detail.component.css',
})
export class ReviewDetailComponent {
  gerritChangeId!: string;

  rawDiff: string = '';

  existedComments: GerritCommentInfo[] = [];

  draftComments: GerritCommentInput[] = [];

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

    this.reviewSvc
      .getExistedComments(this.gerritChangeId)
      .subscribe((comments) => {
        console.log('comments', comments);

        this.existedComments = comments;
      });

    this.reviewSvc
      .getDraftComments(this.gerritChangeId)
      .subscribe((comments) => {
        console.log('draft comments', comments);
        this.draftComments = comments;

        this.loadDiffs();
      });
  }

  private loadDiffs() {
    this.reviewSvc.getChangeDiffs(this.gerritChangeId).subscribe((diff) => {
      this.rawDiff = diff;
      // trigger a re-render
      setTimeout(() => this.render(), 0);
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

    // Insert existing draft comments into the diff table
    this.existedComments.forEach((ec) =>
      this.insertCommentRow(ec, 'published')
    );
    this.draftComments.forEach((dc) => this.insertCommentRow(dc, 'draft'));

    // Hook clicks to create new draft rows
    container
      .querySelectorAll<HTMLElement>('.d2h-code-side-linenumber')
      .forEach((el) => {
        el.addEventListener('click', (evt) => {
          const target = evt.currentTarget as HTMLElement;
          const text = target.textContent?.trim() || '';
          const line = parseInt(text, 10);
          if (isNaN(line)) return;

          const cls = target.classList;
          const side: 'PARENT' | 'REVISION' =
            cls.contains('d2h-ins') ||
            cls.contains('d2h-code-side-linenumber-new')
              ? 'REVISION'
              : 'PARENT';

          const fileHeader = target
            .closest('.d2h-file-wrapper')
            ?.querySelector('.d2h-file-header .d2h-file-name');
          const path = fileHeader?.textContent?.trim() || '';

          const dc: GerritCommentInput = { path, side, line, message: '' };
          this.draftComments.push(dc);
          this.insertCommentRow(dc, 'draft');
        });
      });
  }

  private insertCommentRow(
    commentInfo: GerritCommentInput | GerritCommentInfo,
    mode: 'draft' | 'published'
  ) {
    const container = this.diffContainer.nativeElement;
    const wrappers = Array.from(
      container.querySelectorAll<HTMLElement>('.d2h-file-wrapper')
    );
    const fileEl = wrappers.find(
      (fw) =>
        fw
          .querySelector('.d2h-file-header .d2h-file-name')
          ?.textContent?.trim() === commentInfo.path
    );
    if (!fileEl) return;

    const lineCell = Array.from(
      fileEl.querySelectorAll<HTMLElement>('.d2h-code-side-linenumber')
    ).find((td) => td.textContent?.trim() === String(commentInfo.line));
    if (!lineCell) return;

    const tr = lineCell.closest('tr');
    if (!tr || !tr.parentNode) return;

    const commentTr = document.createElement('tr');
    commentTr.classList.add(
      'tr-comment',
      mode === 'draft' ? 'draft' : 'published'
    );

    const commentTd = document.createElement('td');
    commentTd.colSpan = tr.children.length;

    // build comment-box
    const box = document.createElement('div');
    box.classList.add('comment-box');
    box.addEventListener('click', (e) => e.stopPropagation());

    if (mode === 'published') {
      // published read-only
      const header = document.createElement('div');
      header.classList.add('comment-header');
      header.textContent = `@${
        (commentInfo as any).authorName || 'User'
      } — ${new Date(
        (commentInfo as any).updated || Date.now()
      ).toLocaleString()}`;

      const body = document.createElement('div');
      body.classList.add('comment-body');
      body.textContent = commentInfo.message!;

      box.append(header, body);
    } else {
      // draft editable
      const textarea = document.createElement('textarea');
      textarea.rows = 3;
      textarea.value = commentInfo.message || '';
      textarea.addEventListener(
        'input',
        () => (commentInfo.message = textarea.value)
      );

      const btnGroup = document.createElement('div');
      btnGroup.classList.add('comment-box-buttons');

      const cancelBtn = document.createElement('button');
      cancelBtn.textContent = 'Cancel';
      cancelBtn.addEventListener('click', () =>
        this.cancelComment(commentInfo as GerritCommentInput)
      );

      const saveBtn = document.createElement('button');
      saveBtn.textContent = 'Save';
      saveBtn.addEventListener('click', () =>
        this.saveComment(commentInfo as GerritCommentInput)
      );

      btnGroup.append(cancelBtn, saveBtn);
      box.append(textarea, btnGroup);
    }

    commentTd.appendChild(box);
    commentTr.appendChild(commentTd);
    tr.parentNode.insertBefore(commentTr, tr.nextSibling);
  }

  saveComment(draft: GerritCommentInput) {
    this.reviewSvc
      .postDraftComment(this.gerritChangeId, draft)
      .subscribe((commitInfo) => {
        // remove editor on success
        console.log('Pushed commentInput', draft);
        console.log('commitInfo', commitInfo);
        this.draftComments = this.draftComments.filter((dc) => dc !== draft);
      });
  }

  cancelComment(draft: GerritCommentInput) {
    this.draftComments = this.draftComments.filter((dc) => dc !== draft);
  }
}
