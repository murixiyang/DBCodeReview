export type ReviewStatus =
  | 'Not Submitted'
  | 'Waiting for Review'
  | 'Approved'
  | 'Need Resolve';

export interface ReviewStatusEntity {
  username: string;
  projectId: string;
  commitSha: string;
  reviewStatus: ReviewStatus;
}