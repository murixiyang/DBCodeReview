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
  imports: [NgFor, FormsModule],
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
    this.existedComments.forEach((ec) => this.insertCommentRow(ec));
    this.draftComments.forEach((dc) => this.insertCommentRow(dc));

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
          this.insertCommentRow(dc);
        });
      });
  }

  private insertCommentRow(dc: GerritCommentInput, existingText?: string) {
    const container = this.diffContainer.nativeElement;
    // 1) Find the file wrapper for this path
    const fileWrappers = Array.from(
      container.querySelectorAll<HTMLElement>('.d2h-file-wrapper')
    );
    const fileEl = fileWrappers.find((fw) => {
      const nameEl = fw.querySelector('.d2h-file-header .d2h-file-name');
      return nameEl?.textContent?.trim() === dc.path;
    });
    if (!fileEl) return;

    // 2) Find the <td> whose text matches the line number
    const lineCells = Array.from(
      fileEl.querySelectorAll<HTMLElement>('.d2h-code-side-linenumber')
    );
    const lineCell = lineCells.find(
      (td) => td.textContent?.trim() === '' + dc.line
    );
    if (!lineCell) return;

    // 3) Get the <tr> containing that cell
    const tr = lineCell.closest('tr');
    if (!tr || !tr.parentNode) return;

    // 4) Create a new <tr><td colspan="X">…</td></tr>
    const commentTr = document.createElement('tr');
    const commentTd = document.createElement('td');

    // set colspan to span both sides of diff (find number of columns)
    const colCount = tr.children.length;
    commentTd.setAttribute('colspan', '' + colCount);

    // 5) Build your comment editor inside the TD
    const textarea = document.createElement('textarea');
    textarea.rows = 3;
    textarea.value = existingText ?? dc.message ?? '';
    textarea.addEventListener('input', () => (dc.message = textarea.value));

    const saveBtn = document.createElement('button');
    saveBtn.textContent = existingText ? 'Update' : 'Save';
    saveBtn.addEventListener('click', () => this.saveComment(dc));

    const cancelBtn = document.createElement('button');
    cancelBtn.textContent = 'Cancel';
    cancelBtn.addEventListener('click', () => this.cancelComment(dc));

    commentTd.appendChild(textarea);
    commentTd.appendChild(saveBtn);
    commentTd.appendChild(cancelBtn);
    commentTr.appendChild(commentTd);

    // 6) Insert right after the target row
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
