export interface GerritCommentInfo {
  id: string;
  path: string;
  side?: string;
  line?: number;
  inReplyTo?: string;
  message: string;
  updated: string;
}
