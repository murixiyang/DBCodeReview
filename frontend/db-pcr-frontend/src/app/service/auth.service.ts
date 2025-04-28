import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  user$ = new BehaviorSubject<string | null>(null);

  constructor(private http: HttpClient) {
    this.refreshUser();
  }

  /** Redirect the browser to Spring’s OAuth2-login start */
  login() {
    window.location.href = '/oauth2/authorization/gitlab';
  }

  /** Call the check current user endpoint to bootstrap user info */
  refreshUser() {
    this.http
      .get<string>('/api/user', {
        withCredentials: true,
        responseType: 'text' as 'json',
      })
      .subscribe({
        next: (user) => this.user$.next(user),
        error: () => this.user$.next(null),
      });
  }

  /** Navigate the browser to Spring’s logout endpoint */
  logout() {
    // this causes a full page load → Spring invalidates cookies & session and then 302s you
    window.location.href = '/logout';
  }
}
