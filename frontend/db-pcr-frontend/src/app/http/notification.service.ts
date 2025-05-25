import { Injectable } from '@angular/core';
import { SPRING_URL_NOTIFICATION } from '../service/constant.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NotificationDto } from '../interface/database/notification-dto';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private baseUrl = SPRING_URL_NOTIFICATION;

  constructor(private http: HttpClient) {}

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/unread-count`);
  }

  listAll(): Observable<NotificationDto[]> {
    return this.http.get<NotificationDto[]>(`${this.baseUrl}`);
  }

  markRead(id: number): Observable<void> {
    const params = new HttpParams().set('id', id);
    return this.http.post<void>(`${this.baseUrl}/mark-read`, null, { params });
  }
}
