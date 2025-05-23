import { GitlabCommitDto } from './gitlab-commit-dto';

export interface PseudonymGitlabCommitDto {
  commit: GitlabCommitDto;
  authorPseudonym: String;
}
