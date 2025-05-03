import {
  AfterViewInit,
  Component,
  ElementRef,
  OnInit,
  QueryList,
  ViewChildren,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommitDiffSchema } from '@gitbeaker/rest';
import { GitlabService } from '../http/gitlab.service';
import { Diff2HtmlUI } from 'diff2html/lib/ui/js/diff2html-ui-slim';

@Component({
  selector: 'app-commit-details',
  imports: [NgFor, FormsModule],
  templateUrl: './commit-details.component.html',
  styleUrl: './commit-details.component.css',
})
export class CommitDetailComponent implements OnInit, AfterViewInit {
  projectId: string = '';
  sha: string = '';

  diffList: CommitDiffSchema[] = [];

  // grab all <div #diffContainer> refs
  @ViewChildren('diffContainer', { read: ElementRef })
  diffContainers!: QueryList<ElementRef<HTMLDivElement>>;

  constructor(
    private route: ActivatedRoute,
    private gitLabSvc: GitlabService
  ) {}

  ngOnInit() {
    this.projectId = this.route.snapshot.params['projectId'];
    this.sha = this.route.snapshot.params['sha'];

    this.getDiffList();
  }

  getDiffList() {
    this.gitLabSvc.getCommitDiff(this.projectId, this.sha).subscribe((data) => {
      console.log('Modified files:', data);
      this.diffList = data;
    });
  }

  ngAfterViewInit() {
    // whenever diffList *or* the DOM changes, redraw
    this.diffContainers.changes.subscribe(() => this.renderAllDiffs());
    this.renderAllDiffs();
  }

  private renderAllDiffs() {
    // if we have both diffs and containers, zip them by index
    this.diffContainers.forEach((elRef, idx) => {
      const diff = this.diffList[idx];
      if (!diff) return;

      // build a minimal unified-diff
      const header = [
        `diff --git a/${diff['oldPath']} b/${diff['newPath']}`,
        `--- a/${diff['oldPath']}`,
        `+++ b/${diff['newPath']}`,
      ].join('\n');
      const fullDiff = `${header}\n${diff.diff}`;

      // instantiate the UI wrapper
      const ui = new Diff2HtmlUI(elRef.nativeElement, fullDiff, {
        outputFormat: 'side-by-side',
        drawFileList: false,
        matching: 'lines',
        highlight: true,
      });

      ui.draw();
    });
  }
}
