import { CommitSchema } from '@gitbeaker/rest';
import { ReviewStatus } from './review-status';
import { ChangeInfo } from './gerrit/change-info';

export interface CommitListItem {
  status: ReviewStatus;
  commit: CommitSchema;
}

export interface GerritChangeListItem {
  status: ReviewStatus;
  change: ChangeInfo;
}
