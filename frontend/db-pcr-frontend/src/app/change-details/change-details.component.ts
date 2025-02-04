import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ModiFileInfo } from '../interface/modi-file-info';
import { GerritService } from '../http/gerrit.service';
import { KeyValuePipe, NgClass, NgFor, NgIf } from '@angular/common';
import { DiffInfo, DiffContent, FrontDiffLine } from '../interface/diff-info';

@Component({
  selector: 'app-change-details',
  imports: [NgIf, NgFor, KeyValuePipe, NgClass],
  templateUrl: './change-details.component.html',
  styleUrl: './change-details.component.css',
})
export class ChangeDetailsComponent implements OnInit {
  changeId: string = '';
  // TODO: right now, assume the revisionId is '1' in backend
  revisionId: string = '1';

  modiFileMap: Map<string, ModiFileInfo> = new Map<string, ModiFileInfo>();

  selectedFile: string = '';

  diffContentList: FrontDiffLine[] = [];

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
        originalContent: line,
        changedContent: line,
        originalLineNumber: originalLineNumber + i,
        changedLineNumber: changedLineNumber + i,
        highlightOriginal: false,
        highlightChanged: false,
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
        originalContent: aLines[i] || '',
        changedContent: bLines[i] || '',
        originalLineNumber: aLines[i] ? originalLineNumber + i : undefined,
        changedLineNumber: bLines[i] ? changedLineNumber + i : undefined,
        highlightOriginal: !!aLines[i],
        highlightChanged: !!bLines[i],
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
        originalContent: line,
        changedContent: '',
        originalLineNumber: originalLineNumber + i,
        changedLineNumber: undefined,
        highlightOriginal: true,
        highlightChanged: false,
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
        originalContent: '',
        changedContent: line,
        originalLineNumber: undefined,
        changedLineNumber: changedLineNumber + i,
        highlightOriginal: false,
        highlightChanged: true,
      })
    );
  }

  getDiffClass(diffLine: FrontDiffLine, column: 'left' | 'right'): string {
    if (column === 'left') {
      return diffLine.highlightOriginal ? 'red-line line-color' : '';
    } else {
      return diffLine.highlightChanged ? 'green-line line-color' : '';
    }
  }
}
