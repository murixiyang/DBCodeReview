import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { JsonPipe, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommitDiffSchema } from '@gitbeaker/rest';
import { GitlabService } from '../http/gitlab.service';
import { html } from 'diff2html';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-commit-details',
  imports: [NgIf, NgFor, FormsModule, JsonPipe],
  templateUrl: './commit-details.component.html',
  styleUrl: './commit-details.component.css',
})
export class CommitDetailComponent implements OnInit {
  projectId: string = '';
  sha: string = '';

  diffList: CommitDiffSchema[] = [];

  // Store hte generated HTML for the selected file
  diffHtml: SafeHtml | null = null;

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
    // 1) Build a minimal unified-diff with headers
    const header = [
      `diff --git a/${diff.old_path} b/${diff.new_path}`,
      `--- a/${diff.old_path}`,
      `+++ b/${diff.new_path}`,
    ].join('\n');

    const fullDiff = `${header}\n${diff.diff}`;

    // 1) Generate the raw HTML
    this.diffHtml = html(fullDiff, {
      drawFileList: false,
      matching: 'lines',
      outputFormat: 'side-by-side',
    });

    console.log(diff.diff);

    console.log('HTML:', this.diffHtml);
  }
}
