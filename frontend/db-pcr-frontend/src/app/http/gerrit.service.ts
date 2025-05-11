import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SPRING_URL } from '../service/constant.service';

@Injectable({
  providedIn: 'root',
})
export class GerritService {
  private baseUrl = SPRING_URL;

  constructor(private http: HttpClient) {}

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
