export interface ReviewEntry {
  status: ReviewStatus;
  hash: string;
  message: string;
  date: Date;
}

export type ReviewStatus =
  | 'Waiting for Review'
  | 'Approved'
  | 'Need Resolve'
  | 'Rejected';
