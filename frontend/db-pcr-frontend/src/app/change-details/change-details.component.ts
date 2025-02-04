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
  imports: [NgFor, KeyValuePipe, NgClass],
  templateUrl: './change-details.component.html',
  styleUrl: './change-details.component.css',
})
export class ChangeDetailsComponent implements OnInit {
  changeId: string = '';
  // TODO: right now, assume the revisionId is '1' in backend
  revisionId: string = '1';

  modiFileMap: Map<string, ModiFileInfo> = new Map<string, ModiFileInfo>();

  selectedFile: string = '';

  inlineDiffContent: FrontDiffContent[] = [];
  oringinalContent: FrontDiffContent[] = [];
  changedContent: FrontDiffContent[] = [];

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
    this.inlineDiffContent = [];
    this.oringinalContent = [];
    this.changedContent = [];

    this.gerritService
      .getFileDiff(this.changeId, this.revisionId, filePath)
      .subscribe((diff: DiffInfo) => {
        // Convert into FrontDiffContent
        this.convertDiffContentToTwoPart(diff.content);
      });
  }

  convertDiffContentToTwoPart(diffContent: DiffContent[]) {
    for (let i = 0; i < diffContent.length; i++) {
      let diffBlock = diffContent[i];

      if (diffBlock.a) {
        for (let j = 0; j < diffBlock.a.length; j++) {
          this.oringinalContent.push({
            type: 'a',
            content: diffBlock.a[j],
          });

          this.changedContent.push({
            type: 'b',
            content: '',
          });
        }
      } else if (diffBlock.b) {
        for (let j = 0; j < diffBlock.b.length; j++) {
          this.oringinalContent.push({
            type: 'a',
            content: '',
          });

          this.changedContent.push({
            type: 'b',
            content: diffBlock.b[j],
          });
        }
      } else {
        for (let j = 0; j < diffBlock.ab.length; j++) {
          this.oringinalContent.push({
            type: 'ab',
            content: diffBlock.ab[j],
          });

          this.changedContent.push({
            type: 'ab',
            content: diffBlock.ab[j],
          });
        }
      }
    }
  }
}
