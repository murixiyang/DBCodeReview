import { Component, NgModule, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ModiFileInfo } from '../interface/modi-file-info';
import { GerritService } from '../http/gerrit.service';
import {
  CommonModule,
  KeyValuePipe,
  NgClass,
  NgFor,
  NgIf,
} from '@angular/common';
import { DiffInfo, DiffContent, FrontDiffLine } from '../interface/diff-info';
import { CommentInput } from '../interface/comment-input';
import { FormsModule } from '@angular/forms';
import { CommentBoxComponent } from './comment-box/comment-box.component';
import { CommentInfo } from '../interface/comment-info';

@Component({
  selector: 'app-change-details',
  imports: [
    NgIf,
    NgFor,
    KeyValuePipe,
    NgClass,
    FormsModule,
    CommentBoxComponent,
  ],
  templateUrl: './change-details.component.html',
  styleUrl: './change-details.component.css',
})
export class ChangeDetailsComponent implements OnInit {
  changeId: string = '';
  // TODO: right now, assume the revisionId is '1' in backend
  revisionId: string = '1';

  modiFileMap: Map<string, ModiFileInfo> = new Map<string, ModiFileInfo>();

  selectedFile: string = '';
  selectedSide: 'PARENT' | 'REVISION' | null = null;
  selectedLine: FrontDiffLine | null = null;

  diffContentList: FrontDiffLine[] = [];

  parentDraftCommentMap: Map<number, CommentInfo[]> = new Map();
  revisionDraftCommentMap: Map<number, CommentInfo[]> = new Map();

  constructor(
    private route: ActivatedRoute,
    private gerritService: GerritService
  ) {}

  ngOnInit() {
    this.changeId = this.route.snapshot.params['id'];

    this.getModiFileList(this.changeId, this.revisionId);

    this.fetchDraftComments();
  }

  getModiFileList(changeId: string, revisionId: string) {
    this.gerritService
      .getModifiedFileInChange(changeId, revisionId)
      .subscribe((dataMap: Map<string, ModiFileInfo>) => {
        this.modiFileMap = new Map(Object.entries(dataMap));

        // Filter out commit message
        this.modiFileMap.delete('/COMMIT_MSG');
      });
  }

  getFileDiff(filePath: string) {
    this.selectedFile = filePath;

    this.gerritService
      .getFileDiff(this.changeId, this.revisionId, filePath)
      .subscribe((diff: DiffInfo) => {
        // Convert into FrontDiffContent
        this.convertDiffContentToTwoPart(diff.content);
      });
  }

  fetchDraftComments() {
    this.gerritService
      .getAllDraftComments(this.changeId, this.revisionId)
      .subscribe((dataMap: Map<string, CommentInfo[]>) => {
        const draftCommentMap = new Map(Object.entries(dataMap));
        this.buildDraftCommentList(draftCommentMap);
      });
  }

  buildDraftCommentList(draftCommentMap: Map<string, CommentInfo[]>): void {
    // Clear existing maps so we don't accumulate old data on repeated refreshes
    this.parentDraftCommentMap.clear();
    this.revisionDraftCommentMap.clear();

    // For each file path -> array of comments
    draftCommentMap.forEach((commentList, path) => {
      commentList.forEach((comment) => {
        // If 'side' is null or undefined, default to 'REVISION'
        const side = comment.side ?? 'REVISION';
        comment.path = path;

        // Insert into the correct map based on side
        if (side === 'PARENT') {
          if (!this.parentDraftCommentMap.has(comment.line)) {
            this.parentDraftCommentMap.set(comment.line, []);
          }
          this.parentDraftCommentMap.get(comment.line)?.push(comment);
        } else {
          if (!this.revisionDraftCommentMap.has(comment.line)) {
            this.revisionDraftCommentMap.set(comment.line, []);
          }
          this.revisionDraftCommentMap.get(comment.line)?.push(comment);
        }
      });
    });
  }

  getCommentsForLine(
    lineNumber: number | undefined,
    side: 'PARENT' | 'REVISION'
  ): CommentInfo[] {
    if (lineNumber === undefined) {
      return [];
    }

    if (side === 'PARENT') {
      return this.parentDraftCommentMap.get(lineNumber) || [];
    } else {
      return this.revisionDraftCommentMap.get(lineNumber) || [];
    }
  }

  // When a line is clicked, open comment box below it
  onLineClick(line: FrontDiffLine, column: 'PARENT' | 'REVISION') {
    // Ensure the line have content
    if (
      (column === 'PARENT' && !line.parent_content) ||
      (column === 'REVISION' && !line.revision_content)
    ) {
      return;
    }

    this.selectedLine = line;
    this.selectedSide = column;
  }

  onCommentCancel() {
    this.selectedLine = null;
    this.selectedSide = null;
    this.fetchDraftComments();
    console.log('Refetching draft comments');
    console.log('Draft comments:', this.parentDraftCommentMap);
    console.log('Draft comments:', this.revisionDraftCommentMap);
  }

  private convertDiffContentToTwoPart(diffContent: DiffContent[]) {
    const result: FrontDiffLine[] = [];
    let parentLineNumber = 1;
    let revisionLineNumber = 1;

    for (const diffBlock of diffContent) {
      if (diffBlock.ab) {
        this.processUnrevisionLines(
          diffBlock.ab,
          result,
          parentLineNumber,
          revisionLineNumber
        );
        parentLineNumber += diffBlock.ab.length;
        revisionLineNumber += diffBlock.ab.length;
        continue;
      }

      if (diffBlock.a && diffBlock.b) {
        this.processModifiedLines(
          diffBlock.a,
          diffBlock.b,
          result,
          parentLineNumber,
          revisionLineNumber
        );
        parentLineNumber += diffBlock.a.length;
        revisionLineNumber += diffBlock.b.length;
        continue;
      }

      if (diffBlock.a) {
        this.processDeletedLines(diffBlock.a, result, parentLineNumber);
        parentLineNumber += diffBlock.a.length;
        continue;
      }

      if (diffBlock.b) {
        this.processAddedLines(diffBlock.b, result, revisionLineNumber);
        revisionLineNumber += diffBlock.b.length;
      }
    }

    this.diffContentList = result;
  }

  private processUnrevisionLines(
    lines: string[],
    result: FrontDiffLine[],
    parentLineNumber: number,
    revisionLineNumber: number
  ) {
    lines.forEach((line, i) =>
      result.push({
        parent_content: line,
        revision_content: line,
        parent_line_num: parentLineNumber + i,
        revision_line_num: revisionLineNumber + i,
        highlight_parent: false,
        highlight_revision: false,
      })
    );
  }

  private processModifiedLines(
    aLines: string[],
    bLines: string[],
    result: FrontDiffLine[],
    parentLineNumber: number,
    revisionLineNumber: number
  ) {
    const maxLength = Math.max(aLines.length, bLines.length);

    for (let i = 0; i < maxLength; i++) {
      result.push({
        parent_content: aLines[i] || '',
        revision_content: bLines[i] || '',
        parent_line_num: aLines[i] ? parentLineNumber + i : undefined,
        revision_line_num: bLines[i] ? revisionLineNumber + i : undefined,
        highlight_parent: !!aLines[i],
        highlight_revision: !!bLines[i],
      });
    }
  }

  private processDeletedLines(
    lines: string[],
    result: FrontDiffLine[],
    parentLineNumber: number
  ) {
    lines.forEach((line, i) =>
      result.push({
        parent_content: line,
        revision_content: '',
        parent_line_num: parentLineNumber + i,
        revision_line_num: undefined,
        highlight_parent: true,
        highlight_revision: false,
      })
    );
  }

  private processAddedLines(
    lines: string[],
    result: FrontDiffLine[],
    revisionLineNumber: number
  ) {
    lines.forEach((line, i) =>
      result.push({
        parent_content: '',
        revision_content: line,
        parent_line_num: undefined,
        revision_line_num: revisionLineNumber + i,
        highlight_parent: false,
        highlight_revision: true,
      })
    );
  }

  getDiffClass(diffLine: FrontDiffLine, column: 'PARENT' | 'REVISION'): string {
    var result_class = '';

    if (column === 'PARENT') {
      // Highlight color
      result_class += diffLine.highlight_parent ? 'red-line line-color ' : '';

      // Whether can hover
      result_class += diffLine.parent_content ? 'have-content ' : '';

      // Whether selected
      result_class +=
        this.selectedLine?.parent_line_num === diffLine.parent_line_num &&
        this.selectedSide === 'PARENT'
          ? 'line-selected '
          : '';
    } else {
      // Highlight color
      result_class += diffLine.highlight_revision
        ? 'green-line line-color '
        : '';

      // Whether can hover
      result_class += diffLine.revision_content ? 'have-content ' : '';

      // Whether selected
      result_class +=
        this.selectedLine?.revision_line_num === diffLine.revision_line_num &&
        this.selectedSide === 'REVISION'
          ? 'line-selected '
          : '';
    }

    return result_class;
  }
}
