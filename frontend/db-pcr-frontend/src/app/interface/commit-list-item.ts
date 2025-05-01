import { CommitSchema } from '@gitbeaker/rest';
import { ReviewStatus } from './review-status';

export interface CommitListItem {
  status: ReviewStatus;
  commit: CommitSchema;
}
