import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private credentials: string | null = null;
  private userSubject = new BehaviorSubject<string | null>(null);
  user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient) {
    const stored = sessionStorage.getItem('credentials');
    if (stored) {
      this.credentials = stored;
      const decodedUser = atob(stored).split(':')[0];
      this.userSubject.next(decodedUser);
    }
  }

  /**
   * Perform Basic auth against /api/auth-test.
   * On success, store credentials and emit username.
   */
  login(username: string, password: string): Observable<any> {
    const creds = btoa(`${username}:${password}`);
    const headers = new HttpHeaders({ Authorization: `Basic ${creds}` });
    return this.http
      .get('/api/auth-test', { headers, responseType: 'text' })
      .pipe(
        tap(() => {
          this.credentials = creds;
          sessionStorage.setItem('credentials', creds);
          this.userSubject.next(username);
        })
      );
  }

  /** Clear session and notify subscribers */
  logout() {
    this.credentials = null;
    sessionStorage.removeItem('credentials');
    this.userSubject.next(null);
  }

  /** Provide headers for authenticated requests */
  getAuthHeaders(): HttpHeaders {
    return this.credentials
      ? new HttpHeaders({ Authorization: `Basic ${this.credentials}` })
      : new HttpHeaders();
  }
}
