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
  settingsOpen = false;

  constructor(private authService: AuthService, private router: Router) {
    this.user$ = this.authService.user$;
  }

  /** Log out and redirect to the login page */
  logout() {
    this.authService.logout();
    this.settingsOpen = false;
  }

  /** Navigate to the login page */
  login() {
    this.authService.login();
  }

  toggleSettings() {
    this.settingsOpen = !this.settingsOpen;
  }

  goToMaintenance() {
    this.settingsOpen = false;
    this.router.navigate(['maintain', 'project-list']);
  }
}
