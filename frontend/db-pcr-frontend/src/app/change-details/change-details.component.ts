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
import { CommentInput, CommentRange } from '../interface/comment-input';
import { FormsModule } from '@angular/forms';
import { CommentBoxComponent } from './comment-box/comment-box.component';

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
  draftCommentList: CommentInput[] = [];

  constructor(
    private route: ActivatedRoute,
    private gerritService: GerritService
  ) {}

  ngOnInit() {
    this.changeId = this.route.snapshot.params['id'];

    this.getModiFileList(this.changeId, this.revisionId);
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

  // When a line is clicked, open comment box below it
  onLineClick(line: FrontDiffLine, column: 'PARENT' | 'REVISION') {
    // Ensure the line have content
    if (
      (column === 'PARENT' && !line.original_content) ||
      (column === 'REVISION' && !line.changed_content)
    ) {
      return;
    }

    this.selectedLine = line;
    this.selectedSide = column;
  }

  onCommentCancel() {
    this.selectedLine = null;
    this.selectedSide = null;
  }

  private convertDiffContentToTwoPart(diffContent: DiffContent[]) {
    const result: FrontDiffLine[] = [];
    let originalLineNumber = 1;
    let changedLineNumber = 1;

    for (const diffBlock of diffContent) {
      if (diffBlock.ab) {
        this.processUnchangedLines(
          diffBlock.ab,
          result,
          originalLineNumber,
          changedLineNumber
        );
        originalLineNumber += diffBlock.ab.length;
        changedLineNumber += diffBlock.ab.length;
        continue;
      }

      if (diffBlock.a && diffBlock.b) {
        this.processModifiedLines(
          diffBlock.a,
          diffBlock.b,
          result,
          originalLineNumber,
          changedLineNumber
        );
        originalLineNumber += diffBlock.a.length;
        changedLineNumber += diffBlock.b.length;
        continue;
      }

      if (diffBlock.a) {
        this.processDeletedLines(diffBlock.a, result, originalLineNumber);
        originalLineNumber += diffBlock.a.length;
        continue;
      }

      if (diffBlock.b) {
        this.processAddedLines(diffBlock.b, result, changedLineNumber);
        changedLineNumber += diffBlock.b.length;
      }
    }

    this.diffContentList = result;
  }

  private processUnchangedLines(
    lines: string[],
    result: FrontDiffLine[],
    originalLineNumber: number,
    changedLineNumber: number
  ) {
    lines.forEach((line, i) =>
      result.push({
        original_content: line,
        changed_content: line,
        original_line_num: originalLineNumber + i,
        changed_line_num: changedLineNumber + i,
        highlight_original: false,
        highlight_changed: false,
      })
    );
  }

  private processModifiedLines(
    aLines: string[],
    bLines: string[],
    result: FrontDiffLine[],
    originalLineNumber: number,
    changedLineNumber: number
  ) {
    const maxLength = Math.max(aLines.length, bLines.length);

    for (let i = 0; i < maxLength; i++) {
      result.push({
        original_content: aLines[i] || '',
        changed_content: bLines[i] || '',
        original_line_num: aLines[i] ? originalLineNumber + i : undefined,
        changed_line_num: bLines[i] ? changedLineNumber + i : undefined,
        highlight_original: !!aLines[i],
        highlight_changed: !!bLines[i],
      });
    }
  }

  private processDeletedLines(
    lines: string[],
    result: FrontDiffLine[],
    originalLineNumber: number
  ) {
    lines.forEach((line, i) =>
      result.push({
        original_content: line,
        changed_content: '',
        original_line_num: originalLineNumber + i,
        changed_line_num: undefined,
        highlight_original: true,
        highlight_changed: false,
      })
    );
  }

  private processAddedLines(
    lines: string[],
    result: FrontDiffLine[],
    changedLineNumber: number
  ) {
    lines.forEach((line, i) =>
      result.push({
        original_content: '',
        changed_content: line,
        original_line_num: undefined,
        changed_line_num: changedLineNumber + i,
        highlight_original: false,
        highlight_changed: true,
      })
    );
  }

  getDiffClass(diffLine: FrontDiffLine, column: 'PARENT' | 'REVISION'): string {
    var result_class = '';

    if (column === 'PARENT') {
      result_class += diffLine.highlight_original ? 'red-line line-color ' : '';
      result_class += diffLine.original_content ? 'have-content' : '';
    } else {
      result_class += diffLine.highlight_changed
        ? 'green-line line-color '
        : '';
      result_class += diffLine.changed_content ? 'have-content' : '';
    }
    return result_class;
  }
}
