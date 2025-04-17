import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
} from '@angular/common/http';
import { AuthService } from './auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService) {}

  // adds the Authorization header to every outgoing HTTP request
  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const authHeaders = this.auth.getAuthHeaders();
    const cloned = req.clone({
      headers: req.headers.set(
        'Authorization',
        authHeaders.get('Authorization') || ''
      ),
    });
    return next.handle(cloned);
  }
}
