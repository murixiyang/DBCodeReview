export type ReviewStatus =
  | 'NOT_SUBMITTED'
  | 'WAITING_FOR_REVIEW'
  | 'APPROVED'
  | 'NEED_RESOLVE';

export interface ReviewStatusEntity {
  username: string;
  projectId: string;
  commitSha: string;
  reviewStatus: ReviewStatus;
}
