import { Injectable } from '@angular/core';
import { SPRING_URL_EVALUATION } from '../service/constant.service';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class EvaluationService {
  private baseUrl = SPRING_URL_EVALUATION;

  constructor(private http: HttpClient) {}
}
