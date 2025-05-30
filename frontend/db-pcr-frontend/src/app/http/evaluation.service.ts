import { Injectable } from '@angular/core';
import { SPRING_URL_EVALUATION } from '../service/constant.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FilePayload } from '../interface/eval/file-paylod';
import { EvalReviewDto } from '../interface/eval/eval-review-dto';
import { NamedAuthorCodeDto } from '../interface/eval/named-author-code-dto';

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

  /** Get the evaluation review assignment */
  getEvalReviewAssignment(): Observable<EvalReviewDto> {
    return this.http.get<EvalReviewDto>(
      `${this.baseUrl}/get-eval-review-assignment`
    );
  }

  /** Get the evaluation review assignment */
  getNamedAuthorCode(round: number): Observable<NamedAuthorCodeDto> {
    const params = new HttpParams().set('round', round);

    return this.http.get<NamedAuthorCodeDto>(
      `${this.baseUrl}/get-named-author-code`,
      {
        params,
      }
    );
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
