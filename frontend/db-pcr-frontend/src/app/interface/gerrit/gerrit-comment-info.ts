import { CommentRange } from './comment-range';

export interface GerritCommentInfo {
  id: string;
  path: string;
  side: string;
  line?: number;
  range?: CommentRange;
  inReplyTo?: string;
  message: string;
  updated: string;
}
