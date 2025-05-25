export type ReviewStatus =
  | 'NOT_REVIEWED' // never looked at
  | 'IN_REVIEW' // currently being reviewed (making draft comments)
  | 'WAITING_RESOLVE' // reviewer gave need resolve comments
  | 'APPROVED'; // reviewer gave approved
