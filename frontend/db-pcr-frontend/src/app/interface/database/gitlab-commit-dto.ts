import { ChangeStatus } from '../change-status';

export interface GitlabCommitDto {
  id: number;
  gitlabCommitId: string;
  projectId: number;
  authorId: number;
  message: string;
  committedAt: string;
  fetchedAt: string;
}
