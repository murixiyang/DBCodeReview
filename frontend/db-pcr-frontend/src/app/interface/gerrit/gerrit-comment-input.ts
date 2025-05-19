import { CommentRange } from './comment-range';

export interface GerritCommentInput {
  id?: string; // UUID of the comment, if exists, update comment
  path?: string; // file path
  side?: string; // 'PARENT' or 'REVISION'
  line?: number; // if 0, it's a file comment
  range?: CommentRange;
  in_reply_to?: string; // URL encoded UUID of the comment to which this comment is a reply
  updated?: string; // updated timestamp 'yyyy-mm-dd hh:mm:ss.fffffffff'
  message?: string; // If not set and an existing draft comment, the existing draft comment is deleted.
}
