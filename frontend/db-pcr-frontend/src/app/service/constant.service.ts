import { Injectable } from '@angular/core';

export const SPRING_URL = '/api';
export const SPRING_URL_GITLAB = '/api/gitlab';
export const SPRING_URL_REVIEW = '/api/review';
export const SPRING_URL_MAINTAIN = '/api/maintain';
export const SPRING_URL_NOTIFICATION = '/api/notifications';

@Injectable({
  providedIn: 'root',
})
export class ConstantService {
  constructor() {}
}
