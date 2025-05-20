import {
  ApplicationRef,
  Component,
  ComponentFactoryResolver,
  ElementRef,
  EmbeddedViewRef,
  Injector,
  ViewChild,
} from '@angular/core';
import { Diff2HtmlUIConfig } from 'diff2html/lib/ui/js/diff2html-ui-base.js';
import { Diff2HtmlUI } from 'diff2html/lib/ui/js/diff2html-ui-slim.js';
import { ActivatedRoute } from '@angular/router';
import { ReviewService } from '../../http/review.service.js';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input.js';
import { FormsModule } from '@angular/forms';
import { GerritCommentInfo } from '../../interface/gerrit/gerrit-comment-info.js';
import { CommentBoxComponent } from '../comment-box/comment-box.component.js';

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
    private reviewSvc: ReviewService,
    private appRef: ApplicationRef,
    private resolver: ComponentFactoryResolver,
    private injector: Injector
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
    commentTr.classList.add('tr-comment', mode);
    const commentTd = document.createElement('td');
    commentTd.colSpan = tr.children.length;

    // Append cell into the row
    tr.parentNode!.insertBefore(commentTr, tr.nextSibling);
    commentTr.appendChild(commentTd);

    // Create comment box component
    const factory = this.resolver.resolveComponentFactory(CommentBoxComponent);
    const compRef = factory.create(this.injector);
    compRef.setInput('mode', mode);
    compRef.setInput('comment', commentInfo);

    // Listen for events
    compRef.instance.saved.subscribe((c: GerritCommentInput) => {
      this.saveComment(c);
    });
    compRef.instance.canceled.subscribe((c: GerritCommentInput) => {
      this.cancelComment(c);
    });

    // Attach to Angular change detection
    this.appRef.attachView(compRef.hostView);

    // Get the DOM node and append
    const domElem = (compRef.hostView as EmbeddedViewRef<any>)
      .rootNodes[0] as HTMLElement;
    commentTd.appendChild(domElem);

    // Kick off change detection on that component
    compRef.changeDetectorRef.detectChanges();
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
