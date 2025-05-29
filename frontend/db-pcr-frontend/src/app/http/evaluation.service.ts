import { Injectable } from '@angular/core';
import { SPRING_URL_EVALUATION } from '../service/constant.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FilePayload } from '../interface/eval/file-paylod';

@Injectable({
  providedIn: 'root',
})
export class EvaluationService {
  private baseUrl = SPRING_URL_EVALUATION;

  constructor(private http: HttpClient) {}

  /** Get project commits with CommitStatus */
  getTemplateDownloaded(language: string): Observable<Blob> {
    const params = new HttpParams().set('language', language);

    return this.http.get(`${this.baseUrl}/get-template`, {
      params,
      responseType: 'blob',
    });
  }

  /** Publish the files author uploaded to gerrit */
  publishToGerrit(language: string, files: FilePayload[]): Observable<string> {
    const params = new HttpParams().set('language', language);
    return this.http.post(`${this.baseUrl}/publish-to-gerrit`, files, {
      params,
      responseType: 'text',
    });
  }
}
