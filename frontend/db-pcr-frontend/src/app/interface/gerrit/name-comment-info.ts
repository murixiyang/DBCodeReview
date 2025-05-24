import { GerritCommentInfo } from './gerrit-comment-info';

export interface NameCommentInfo {
  displayName: string;
  isAuthor: boolean;
  commentInfo: GerritCommentInfo;
}
