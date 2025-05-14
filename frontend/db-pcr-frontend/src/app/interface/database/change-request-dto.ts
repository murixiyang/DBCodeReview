import { ChangeStatus } from "../change-status";

export interface ChangeRequestDto {
  id: number;
  assignmentId: number;
  commitId: number;
  gerritChangeId: string;
  submittedAt: string;
  status: ChangeStatus;
  lastStatusAt: string;
}
