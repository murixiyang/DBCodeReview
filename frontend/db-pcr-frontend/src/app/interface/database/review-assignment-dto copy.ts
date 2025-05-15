import { ChangeStatus } from '../change-status';

export interface ReviewAssignmentUsernameDto {
  id: number;
  authorName: string;
  reviewerName: string;
  projectId: number;
  assignedAt: string;
  projectStatus: ChangeStatus;
  projectStatusAt: string;
}
