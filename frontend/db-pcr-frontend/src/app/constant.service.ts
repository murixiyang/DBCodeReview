import { Injectable } from '@angular/core';

export const SPRING_URL = 'http://localhost:8081/api';

@Injectable({
  providedIn: 'root',
})
export class ConstantService {
  constructor() {}
}
