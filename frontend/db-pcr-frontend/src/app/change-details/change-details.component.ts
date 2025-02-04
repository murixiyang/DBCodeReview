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

  diffContentPair: FrontDiffLine[] = [];

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
    this.diffContentPair = [];

    this.gerritService
      .getFileDiff(this.changeId, this.revisionId, filePath)
      .subscribe((diff: DiffInfo) => {
        // Convert into FrontDiffContent
        this.convertDiffContentToTwoPart(diff.content);
      });
  }

  private convertDiffContentToTwoPart(diffContent: DiffContent[]) {
    for (let i = 0; i < diffContent.length; i++) {
      let diffBlock = diffContent[i];

      // If have ab, then a and b should be empty
      if (diffBlock.ab) {
        for (let j = 0; j < diffBlock.ab.length; j++) {
          this.diffContentPair.push({
            originalContent: diffBlock.ab[j],
            changedContent: diffBlock.ab[j],
            highlightOrignial: false,
            highlightChanged: false,
          });
        }
        continue;
      }

      // Both have content, but not the same
      if (diffBlock.a && diffBlock.b) {
        var maxLength = Math.max(diffBlock.a.length, diffBlock.b.length);

        for (let j = 0; j < maxLength; j++) {
          var highlightOrignial = diffBlock.a[j] ? true : false;
          var highlightChanged = diffBlock.b[j] ? true : false;

          this.diffContentPair.push({
            originalContent: highlightOrignial ? diffBlock.a[j] : '',
            changedContent: highlightChanged ? diffBlock.b[j] : '',
            highlightOrignial: highlightOrignial,
            highlightChanged: highlightChanged,
          });
        }

        continue;
      }

      // Only one side has content
      if (diffBlock.a) {
        for (let j = 0; j < diffBlock.a.length; j++) {
          this.diffContentPair.push({
            originalContent: diffBlock.a[j],
            changedContent: '',
            highlightOrignial: true,
            highlightChanged: false,
          });
        }
      } else if (diffBlock.b) {
        for (let j = 0; j < diffBlock.b.length; j++) {
          this.diffContentPair.push({
            originalContent: '',
            changedContent: diffBlock.b[j],
            highlightOrignial: false,
            highlightChanged: true,
          });
        }
      }
    }
  }

  getDiffClass(diffLine: FrontDiffLine, column: 'left' | 'right'): string {
    if (column === 'left') {
      return diffLine.highlightOrignial ? 'red-line line-color' : '';
    } else {
      return diffLine.highlightChanged ? 'green-line line-color' : '';
    }
  }
}
