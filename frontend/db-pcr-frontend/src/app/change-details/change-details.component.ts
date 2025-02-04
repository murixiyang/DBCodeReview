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
      result_class += diffLine.highlight_parent ? 'red-line line-color ' : '';
      result_class += diffLine.parent_content ? 'have-content' : '';
    } else {
      result_class += diffLine.highlight_revision
        ? 'green-line line-color '
        : '';
      result_class += diffLine.revision_content ? 'have-content' : '';
    }
    return result_class;
  }
}
