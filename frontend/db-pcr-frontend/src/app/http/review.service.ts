import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { SPRING_URL_REVIEW } from '../service/constant.service';
import { Observable } from 'rxjs';
import { ProjectDto } from '../interface/database/project-dto';
import { ChangeRequestDto } from '../interface/database/change-request-dto';
import { ReviewAssignmentPseudonymDto } from '../interface/database/review-assignment-dto';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private baseUrl = SPRING_URL_REVIEW;

  constructor(private http: HttpClient) {}

  /** Get the list of projects that user being assigned as reviewer */
  getProjectsToReview(): Observable<ProjectDto[]> {
    return this.http.get<ProjectDto[]>(
      `${this.baseUrl}/get-projects-to-review`
    );
  }

  /** Get ChangeRequest Dto for a project */
  getChangeRequestForProject(
    groupProjectId: string
  ): Observable<ChangeRequestDto[]> {
    const params = new HttpParams().set('groupProjectId', groupProjectId);

    return this.http.get<ChangeRequestDto[]>(
      `${this.baseUrl}/get-review-project-commits`,
      {
        params,
      }
    );
  }

  /** Get ReviewAssignment Pseudunym Dto by assignment id */
  getReviewAssignmentPseudonymDtoList(
    groupProjectId: string
  ): Observable<ReviewAssignmentPseudonymDto[]> {
    const params = new HttpParams().set(
      'groupProjectId',
      groupProjectId.toString()
    );

    return this.http.get<ReviewAssignmentPseudonymDto[]>(
      `${this.baseUrl}/get-review-assignment-pseudonym`,
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
    const params = new HttpParams().set('projectId', projectId).set('sha', sha);
    return this.http.post<{ changeId: string }>(
      `${this.baseUrl}/post-request-review`,
      null,
      { params }
    );
  }

  /** Get assignment metadata for Uuid */
  // getAssignmentMetaByUuid(
  //   assignmentUuid: string
  // ): Observable<AssignmentMetadata> {
  //   const params = new HttpParams().set('assignmentUuid', assignmentUuid);

  //   return this.http.get<AssignmentMetadata>(
  //     `${this.baseUrl}/get-metadata-by-uuid`,
  //     {
  //       params,
  //     }
  //   );
  // }

  /** Get Gerrit ChangeInfo list by Uuid */
  // getGerritChangeInfoByUuid(assignmentUuid: string): Observable<ChangeInfo[]> {
  //   const params = new HttpParams().set('assignmentUuid', assignmentUuid);

  //   return this.http.get<ChangeInfo[]>(
  //     `${this.baseUrl}/get-commit-list-by-uuid`,
  //     {
  //       params,
  //     }
  //   );
  // }

  /** Get ChangeDiff for */
  getChangeDiffs(gerritChangeId: string): Observable<string> {
    const params = new HttpParams().set('gerritChangeId', gerritChangeId);

    return this.http.get(`${this.baseUrl}/get-change-diff`, {
      params,
      responseType: 'text',
    });
  }
}
