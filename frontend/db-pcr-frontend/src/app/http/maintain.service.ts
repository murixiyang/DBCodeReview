import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { SPRING_URL_MAINTAIN } from '../service/constant.service';

@Injectable({
  providedIn: 'root',
})
export class MaintainService {
  private baseUrl = SPRING_URL_MAINTAIN;

  constructor(private http: HttpClient) {}
}
