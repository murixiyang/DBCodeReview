export interface CommentInput {
  draft_id: string; // UUID of the comment, if exists, update comment
  file_path: string;
  side: 'PARENT' | 'REVISION'; // 'PARENT' or 'REVISION'
  line_num?: number; // if 0, it's a file comment
  line_range?: CommentRange;
  reply_comment_id?: string; // UUID of the comment it replies to
  updated_time?: string; // updated timestamp 'yyyy-mm-dd hh:mm:ss.fffffffff'
  message?: string; // If not set and an existing draft comment, the existing draft comment is deleted.
}

export interface JsonCommentInput {
  id: string;
  path: string;
  side: 'PARENT' | 'REVISION';
  line?: number;
  range?: CommentRange;
  in_reply_to?: string;
  updated?: string;
  message?: string;
}

export interface CommentRange {
  // Line number 1-based, character number 0-based
  start_line: number;
  start_character: number;
  end_line: number;
  end_character: number;
}
