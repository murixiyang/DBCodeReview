import { ReviewStatus } from '../status/review-status';

export interface ChangeRequestDto {
  id: number;
  assignmentId: number;
  commitId: number;
  gerritChangeId: string;
  message: string;
  submittedAt: string;
  status: ReviewStatus;
  lastStatusAt: string;
}
