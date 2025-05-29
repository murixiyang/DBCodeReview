import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-eval-intro',
  imports: [],
  templateUrl: './eval-intro.component.html',
  styleUrl: './eval-intro.component.css',
})
export class EvalIntroComponent {
  constructor(private router: Router) {}

  onStart() {
    this.router.navigate(['/eval/author']);
  }
}
