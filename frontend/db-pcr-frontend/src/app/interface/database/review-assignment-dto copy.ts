import { ProjectStatus } from '../status/project-status';

export interface ReviewAssignmentUsernameDto {
  id: number;
  authorName: string;
  reviewerName: string;
  projectId: number;
  assignedAt: string;
  projectStatus: ProjectStatus;
  projectStatusAt: string;
}
