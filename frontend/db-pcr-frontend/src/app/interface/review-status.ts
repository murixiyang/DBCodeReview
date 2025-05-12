export type ReviewStatus =
  | 'NOT_SUBMITTED'
  | 'WAITING_FOR_REPLY'
  | 'APPROVED'
  | 'NEED_RESOLVE';

export interface ReviewStatusEntity {
  username: string;
  projectId: string;
  commitSha: string;
  reviewStatus: ReviewStatus;
}
