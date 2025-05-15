import { ProjectStatus } from "../status/project-status";

export interface ReviewAssignmentPseudonymDto {
  id: number;
  authorId: number;
  reviewerId: number;
  projectId: number;
  assignedAt: string;
  projectStatus: ProjectStatus;
  projectStatusAt: string;
}
