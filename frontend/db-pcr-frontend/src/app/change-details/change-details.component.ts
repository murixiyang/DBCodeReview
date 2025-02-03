import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ModiFileInfo } from '../interface/modi-file-info';
import { GerritService } from '../http/gerrit.service';
import { KeyValuePipe, NgClass, NgFor, NgIf } from '@angular/common';
import { DiffContent, DiffInfo, FrontDiffConent } from '../interface/diff-info';

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
  diffContent: FrontDiffConent[] = [];

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
    this.diffContent = [];

    this.gerritService
      .getFileDiff(this.changeId, this.revisionId, filePath)
      .subscribe((diff: DiffInfo) => {
        // Convert into FrontDiffContent
        for (let i = 0; i < diff.content.length; i++) {
          let diffContent = diff.content[i];
          let frontDiffContent: FrontDiffConent = {
            type: 'a',
            content: [],
          };

          if (diffContent.a) {
            frontDiffContent.content = diffContent.a;
          } else if (diffContent.b) {
            frontDiffContent.type = 'b';
            frontDiffContent.content = diffContent.b;
          } else {
            frontDiffContent.type = 'ab';
            frontDiffContent.content = diffContent.ab;
          }

          this.diffContent.push(frontDiffContent);
        }
      });
  }
}
