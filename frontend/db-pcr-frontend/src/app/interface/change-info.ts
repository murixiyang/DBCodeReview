import { AccountInfo } from './account-info';

export interface ChangeInfo {
  id: string;
  triplet_id: string;
  project: string;
  branch: string;
  change_id: string;
  subject: string;
  status: string;
  created: string;
  updated: string;
  insertions: number;
  deletions: number;
  owner: AccountInfo;
}
