import { Injectable } from '@angular/core';

export const SPRING_URL = '/api';
export const SPRING_URL_GITLAB = '/api/gitlab';
export const SPRING_URL_REVIEW = '/api/review';
export const SPRING_URL_MAINTAIN = '/api/maintain';

@Injectable({
  providedIn: 'root',
})
export class ConstantService {
  constructor() {}
}
