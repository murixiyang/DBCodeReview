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
  owner: Owner;
}

export interface Owner {
  _account_id: number;
}
