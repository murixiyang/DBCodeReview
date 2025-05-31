import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-eval-intro',
  imports: [],
  templateUrl: './eval-intro.component.html',
  styleUrl: './eval-intro.component.css',
})
export class EvalIntroComponent {
  constructor(private router: Router, private authSvc: AuthService) {}

  onStart() {
    if (!this.authSvc.user$.getValue()) {
      // If not logged in, redirect to login
      this.authSvc.login();
      return;
    } else {
      this.router.navigate(['/eval/author']);
    }
  }

  getStartText(): string {
    return this.authSvc.user$.getValue()
      ? 'Start Evaluation'
      : 'Register / Login';
  }
}
