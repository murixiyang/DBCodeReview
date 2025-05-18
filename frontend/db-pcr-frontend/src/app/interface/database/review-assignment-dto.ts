import { ProjectStatus } from '../status/project-status';

export interface ReviewAssignmentPseudonymDto {
  id: number;
  authorPseudonym: string;
  reviewerPseudonym: string;
  groupProjectId: number;
  assignedAt: string;
  projectStatus: ProjectStatus;
  projectStatusAt: string;
}
