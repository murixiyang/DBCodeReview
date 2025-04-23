import { Injectable } from '@angular/core';

export const SPRING_URL = '/api';
export const SPRING_URL_GITLAB = '/api/gitlab';

@Injectable({
  providedIn: 'root',
})
export class ConstantService {
  constructor() {}
}
