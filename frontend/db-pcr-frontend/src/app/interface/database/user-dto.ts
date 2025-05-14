import { ChangeStatus } from '../change-status';

export interface UserDto {
  id: number;
  gitlabUserId: number;
  username: string;
  createdAt: string;
}
