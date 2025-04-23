import { Component } from '@angular/core';
import { AuthService } from '../service/auth.service';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [FormsModule, NgIf],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  username = '';
  password = '';
  error: string | null = null;

  constructor(private auth: AuthService, private router: Router) {}

  login() {
    this.error = null;
    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        // credentials are set in AuthService
        this.router.navigate(['/project-list']); // or whatever your list path is
      },
      error: () => {
        this.error = 'Login failed';
      },
    });
  }
}
