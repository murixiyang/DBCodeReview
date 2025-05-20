export interface CommentRange {
  // Line number 1-based, character number 0-based
  startLine: number;
  startCharacter: number;
  endLine: number;
  endCharacter: number;
}
