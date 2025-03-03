import { Injectable } from '@angular/core';

export const SPRING_URL = 'http://localhost:8081/api';
export const SPRING_URL_GITLAB = 'http://localhost:8081/api/gitlab';

@Injectable({
  providedIn: 'root',
})
export class ConstantService {
  constructor() {}
}
