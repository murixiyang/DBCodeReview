import { Component } from '@angular/core';
import { AuthService } from '../service/auth.service';
import { Router, RouterLink } from '@angular/router';
import { AsyncPipe, NgIf } from '@angular/common';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-topbar',
  imports: [NgIf, AsyncPipe, RouterLink],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.css',
})
export class TopbarComponent {
  user$!: Observable<string | null>;

  constructor(private authService: AuthService, private router: Router) {
    this.user$ = this.authService.user$;
  }

  /** Log out and redirect to the login page */
  logout() {
    this.authService.logout();
  }

  /** Navigate to the login page */
  login() {
    this.authService.login();
  }
}
