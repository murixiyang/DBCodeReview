import { ChangeStatus } from '../change-status';

export interface ProjectUserPseudonymDto {
  id: number;
  projectId: number;
  userId: number;
  role: string;
  pseudonymName: string;
  assignedAt: string;
}

