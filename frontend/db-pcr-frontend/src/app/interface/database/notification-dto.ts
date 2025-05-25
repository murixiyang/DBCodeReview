import { NotificationType } from '../notification-type';

export interface NotificationDto {
  type: NotificationType;
  link: string;
  message: string;
  seen: boolean;
  createdAt: string;
}
