import { ChangeStatus } from '../change-status';

export interface ReviewAssignmentDto {
  id: number;
  authorId: number;
  reviewerId: number;
  projectId: number;
  assignedAt: string;
  projectStatus: ChangeStatus;
  projectStatusAt: string;
}
