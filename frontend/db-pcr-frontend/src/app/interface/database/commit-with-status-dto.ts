import { CommitStatus } from '../status/commit-status';
import { GitlabCommitDto } from './gitlab-commit-dto';

export interface CommitWithStatusDto {
  commit: GitlabCommitDto;
  status: CommitStatus;
}
