import { NotificationType } from '../notification-type';

export interface NotificationDto {
  id: number;
  type: NotificationType;
  link: string;
  message: string;
  seen: boolean;
  createdAt: string;
}
