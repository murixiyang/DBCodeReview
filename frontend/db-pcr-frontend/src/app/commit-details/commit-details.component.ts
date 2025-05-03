import {
  AfterViewChecked,
  Component,
  ElementRef,
  OnInit,
  ViewChild,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommitDiffSchema } from '@gitbeaker/rest';
import { GitlabService } from '../http/gitlab.service';
import { html } from 'diff2html';
import { Diff2HtmlUI } from 'diff2html/lib/ui/js/diff2html-ui-slim';

import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-commit-details',
  imports: [NgFor, FormsModule],
  templateUrl: './commit-details.component.html',
  styleUrl: './commit-details.component.css',
})
export class CommitDetailComponent implements OnInit {
  projectId: string = '';
  sha: string = '';

  diffList: CommitDiffSchema[] = [];

  // Store hte generated HTML for the selected file
  diffHtml: SafeHtml | null = null;

  @ViewChild('diffContainer', { static: false })
  diffContainer!: ElementRef<HTMLElement>;

  constructor(
    private route: ActivatedRoute,
    private gitLabSvc: GitlabService,
    private sanitizer: DomSanitizer
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

  onSelectFile(diff: CommitDiffSchema) {
    // Build minimal unified-diff with headers
    const header = [
      `diff --git a/${diff['oldPath']} b/${diff['newPath']}`,
      `--- a/${diff['oldPath']}`,
      `+++ b/${diff['newPath']}`,
    ].join('\n');

    const fullDiff = `${header}\n${diff.diff}`;

    // 1) Draw into your container
    const ui = new Diff2HtmlUI(this.diffContainer.nativeElement, fullDiff, {
      outputFormat: 'side-by-side',
      drawFileList: false,
      matching: 'lines',
    });
    ui.draw();

    // 2) This wrapper has its own highlight step
    ui.highlightCode();
  }
}
