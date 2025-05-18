import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { SPRING_URL_MAINTAIN } from '../service/constant.service';
import { Observable } from 'rxjs';
import { ReviewAssignmentPseudonymDto } from '../interface/database/review-assignment-dto';
import { ReviewAssignmentUsernameDto } from '../interface/database/review-assignment-dto copy';

@Injectable({
  providedIn: 'root',
})
export class MaintainService {
  private baseUrl = SPRING_URL_MAINTAIN;

  constructor(private http: HttpClient) {}

  /** To generate the list of maintainers */
  assignReviewers(
    groupGitlabProjectId: number,
    reviewerNum: number
  ): Observable<ReviewAssignmentUsernameDto[]> {
    const params = new HttpParams()
      .set('groupGitlabProjectId', groupGitlabProjectId.toString())
      .set('reviewerNum', reviewerNum.toString());

    return this.http.post<ReviewAssignmentUsernameDto[]>(
      `${this.baseUrl}/assign`,
      null,
      {
        params,
      }
    );
  }

  /** Get the list of assignments for a project with real username */
  getAssignedList(
    groupGitlabProjectId: number
  ): Observable<ReviewAssignmentUsernameDto[]> {
    const params = new HttpParams().set(
      'groupGitlabProjectId',
      groupGitlabProjectId.toString()
    );

    return this.http.get<ReviewAssignmentUsernameDto[]>(
      `${this.baseUrl}/get-assigned-list`,
      {
        params,
      }
    );
  }
}
