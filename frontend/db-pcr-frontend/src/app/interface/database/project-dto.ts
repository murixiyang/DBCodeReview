import { ChangeStatus } from '../change-status';

export interface ProjectDto {
  id: number;
  gitlabProjectId: number;
  name: string;
  namespace: string;
  ownerId: number;
  groupId: number;
  parentProjectId: number;
  createdAt: string;
}
