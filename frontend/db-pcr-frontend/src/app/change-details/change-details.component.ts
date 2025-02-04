import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ModiFileInfo } from '../interface/modi-file-info';
import { GerritService } from '../http/gerrit.service';
import { KeyValuePipe, NgClass, NgFor, NgIf } from '@angular/common';
import {
  FrontDiffContent,
  DiffInfo,
  DiffContent,
} from '../interface/diff-info';

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

  // inlineDiffContent: FrontDiffContent[] = [];
  // oringinalContent: FrontDiffContent[] = [];
  // changedContent: FrontDiffContent[] = [];
  diffContentPair: FrontDiffContent[] = [];

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

      if (diffBlock.a) {
        for (let j = 0; j < diffBlock.a.length; j++) {
          this.diffContentPair.push({
            originalContent: diffBlock.a[j],
            changedContent: '',
          });
        }
      } else if (diffBlock.b) {
        for (let j = 0; j < diffBlock.b.length; j++) {
          this.diffContentPair.push({
            originalContent: '',
            changedContent: diffBlock.b[j],
          });
        }
      } else {
        for (let j = 0; j < diffBlock.ab.length; j++) {
          this.diffContentPair.push({
            originalContent: diffBlock.ab[j],
            changedContent: diffBlock.ab[j],
          });
        }
      }
    }
  }

  getDiffClass(diffLine: FrontDiffContent, column: 'left' | 'right'): string {
    if (column === 'left') {
      // Original content column: red if changed is empty and original is non-empty
      return diffLine.changedContent === '' && diffLine.originalContent !== ''
        ? 'red-line'
        : '';
    } else {
      // Changed content column: green if original is empty and changed is non-empty
      return diffLine.originalContent === '' && diffLine.changedContent !== ''
        ? 'green-line'
        : '';
    }
  }
}
