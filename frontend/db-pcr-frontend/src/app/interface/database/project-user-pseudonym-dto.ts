export interface ProjectUserPseudonymDto {
  id: number;
  projectId: number;
  userId: number;
  role: string;
  pseudonymName: string;
  assignedAt: string;
}
