import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { SPRING_URL_REVIEW } from '../service/constant.service';
import { Observable } from 'rxjs';
import { ProjectSchema } from '@gitbeaker/rest';
import { AssignmentMetadata } from '../interface/assignment-metadata';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private baseUrl = SPRING_URL_REVIEW;

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

  /** Get assignment metadata for reviewers */
  getAssignmentMetadata(username: string): Observable<AssignmentMetadata[]> {
    const params = new HttpParams().set('reviewerName', username);

    return this.http.get<AssignmentMetadata[]>(
      `${this.baseUrl}/get-assignment-metadata`,
      {
        params,
      }
    );
  }
}
