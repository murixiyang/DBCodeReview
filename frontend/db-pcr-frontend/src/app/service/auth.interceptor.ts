import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
} from '@angular/common/http';
import { AuthService } from './auth.service';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService) {}

  intercept(
    req: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    // Grab the headers from your AuthService (or null)
    const authHeaders = this.auth.getAuthHeaders();

    // Clone the request, adding the Authorization header if present
    const authReq = authHeaders.get('Authorization')
      ? req.clone({
          headers: req.headers.set(
            'Authorization',
            authHeaders.get('Authorization')!
          ),
        })
      : req;

    return next.handle(authReq);
  }
}
