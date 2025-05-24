import { GerritCommentInfo } from './gerrit-comment-info';

export interface UsernameCommentInfo {
  username: string;
  isAuthor: boolean;
  commentInfo: GerritCommentInfo;
}
