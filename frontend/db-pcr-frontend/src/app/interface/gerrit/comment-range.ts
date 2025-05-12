export interface CommentRange {
  // Line number 1-based, character number 0-based
  start_line: number;
  start_character: number;
  end_line: number;
  end_character: number;
}
