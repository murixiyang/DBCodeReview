import { AsyncPipe, DatePipe, NgFor, NgIf } from '@angular/common';
import { Component } from '@angular/core';
import { interval, Observable, startWith, switchMap } from 'rxjs';
import { NotificationDto } from '../interface/database/notification-dto';
import { NotificationService } from '../http/notification.service';
import { Router } from '@angular/router';

import {
  FaIconComponent,
  FaIconLibrary,
} from '@fortawesome/angular-fontawesome';
import { faBell } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-notification-bell',
  imports: [NgIf, AsyncPipe, NgFor, DatePipe, FaIconComponent],
  templateUrl: './notification-bell.component.html',
  styleUrl: './notification-bell.component.css',
})
export class NotificationBellComponent {
  unreadCount$!: Observable<number>;
  notifications$!: Observable<NotificationDto[]>;
  open = false;

  constructor(
    private notifSvc: NotificationService,
    private router: Router,
    lib: FaIconLibrary
  ) {
    lib.addIcons(faBell);
  }

  ngOnInit() {
    // re-poll every 30s, starting immediately
    this.unreadCount$ = interval(30_000).pipe(
      startWith(0),
      switchMap(() => this.notifSvc.getUnreadCount())
    );

    // you can poll the full list too, if you want
    this.notifications$ = interval(60_000).pipe(
      startWith(0),
      switchMap(() => this.notifSvc.listAll())
    );
  }

  toggleDropdown() {
    this.open = !this.open;
  }

  goto(n: NotificationDto) {
    this.notifSvc.markRead(n.id).subscribe(() => {
      this.unreadCount$ = this.notifSvc.getUnreadCount();
      this.notifications$ = this.notifSvc.listAll();
      this.open = false;

      // normalize so we always have a leading "/"
      const link = n.link.startsWith('/') ? n.link : `/${n.link}`;

      if (this.router.url === link) {
        console.log('Reloading page for notification link:', link);
        window.location.reload();
      } else {
        console.log('Navigating to notification link:', link);
        console.log('Current router URL:', this.router.url);
        this.router.navigateByUrl(link);
      }
    });
  }
}
