import { GerritCommentInfo } from './gerrit-comment-info';

export interface PseudonymCommentInfo {
  pseudonym: string;
  isAuthor: boolean;
  commentInfo: GerritCommentInfo;
}
