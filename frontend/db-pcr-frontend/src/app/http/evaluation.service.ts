import { Injectable } from '@angular/core';
import { SPRING_URL_EVALUATION } from '../service/constant.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class EvaluationService {
  private baseUrl = SPRING_URL_EVALUATION;

  constructor(private http: HttpClient) {}

  /** Get project commits with CommitStatus */
  getTemplateDownloaded(language: String): Observable<any> {
    return this.http.get(`${this.baseUrl}/get-template?language=${language}`, {
      responseType: 'blob',
    });
  }

  /** Publish the files author uploaded to gerrit */
  publishToGerrit(payload: { language: any; files: any }): Observable<any> {
    return this.http.post(`${this.baseUrl}/publish-to-gerrit`, payload);
  }
}
