import { ReactState } from '../react-state';

export interface GerritCommentInfo {
  id: string;
  path: string;
  side?: string;
  line?: number;
  inReplyTo?: string;
  message: string;
  thumbState?: ReactState;
  updated: string;
}
