import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { SPRING_URL_MAINTAIN } from '../service/constant.service';
import { Observable } from 'rxjs';
import { ReviewAssignment } from '../interface/review-assignment';
import { ProjectSchema } from '@gitbeaker/rest';

@Injectable({
  providedIn: 'root',
})
export class MaintainService {
  private baseUrl = SPRING_URL_MAINTAIN;

  constructor(private http: HttpClient) {}

  /** To generate the list of maintainers */
  assignReviewers(
    projectId: number,
    reviewerNum: number
  ): Observable<ReviewAssignment[]> {
    const params = new HttpParams()
      .set('projectId', projectId.toString())
      .set('reviewerNum', reviewerNum.toString());

    return this.http.post<ReviewAssignment[]>(`${this.baseUrl}/assign`, null, {
      params,
    });
  }

  /** Get the list of assignments for a project */
  getAssignedList(projectId: number): Observable<ReviewAssignment[]> {
    const params = new HttpParams().set('projectId', projectId.toString());

    return this.http.get<ReviewAssignment[]>(
      `${this.baseUrl}/get-assigned-list`,
      {
        params,
      }
    );
  }

  /** Get the list of projects that user being assigned as reviewer */
  getProjectsToReview(username: string): Observable<ProjectSchema[]> {
    const params = new HttpParams().set('username', username);

    return this.http.get<ProjectSchema[]>(
      `${this.baseUrl}/projects-to-review`,
      {
        params,
      }
    );
  }
}
