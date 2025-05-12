import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SPRING_URL } from '../service/constant.service';
import { ProjectSchema } from '@gitbeaker/rest';

@Injectable({
  providedIn: 'root',
})
export class GerritService {
  private baseUrl = SPRING_URL;

  constructor(private http: HttpClient) {}

  /** Get the list of projects that user being assigned as reviewer */
  getProjectsToReview(username: string): Observable<ProjectSchema[]> {
    const params = new HttpParams().set('username', username);

    return this.http.get<ProjectSchema[]>(
      `${this.baseUrl}/get-projects-to-review`,
      {
        params,
      }
    );
  }

  /** Post a request review from Gitlab commit to Gerrit */
  postRequestReview(
    projectId: string,
    sha: string
  ): Observable<{ changeId: string }> {
    const url = `${this.baseUrl}/post-request-review`;
    // build the body exactly as your @RequestBody record expects
    const body = { projectId, sha };

    return this.http.post<{ changeId: string }>(url, body, {
      withCredentials: true, // send the JSESSIONID so Spring can look up the OAuth2AuthorizedClient
    });
  }
}
