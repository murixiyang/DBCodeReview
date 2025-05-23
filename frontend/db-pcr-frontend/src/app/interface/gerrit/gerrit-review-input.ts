import { GerritCommentInput } from './gerrit-comment-input';

export interface GerritReviewInput {
  message?: string;
  comments?: GerritCommentInput[];
}
