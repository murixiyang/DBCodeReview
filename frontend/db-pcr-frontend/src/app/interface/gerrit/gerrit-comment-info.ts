import { AccountInfo } from './account-info';
import { CommentRange } from './comment-range';

export interface GerritCommentInfo {
  id: string;
  path: string;
  side: string;
  line?: number;
  range?: CommentRange;
  in_reply_to?: string;
  message: string;
  updated: string;
  author?: AccountInfo; // unset for draft comments
}
