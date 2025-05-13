import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { SPRING_URL_REVIEW } from '../service/constant.service';
import { Observable } from 'rxjs';
import { ProjectSchema } from '@gitbeaker/rest';
import { AssignmentMetadata } from '../interface/assignment-metadata';
import { ChangeInfo } from '../interface/gerrit/change-info';
import { ChangeDiff } from '../interface/gerrit/change-diff.ts';

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
  getAssignmentMetaForReviewer(
    username: string
  ): Observable<AssignmentMetadata[]> {
    const params = new HttpParams().set('reviewerName', username);

    return this.http.get<AssignmentMetadata[]>(
      `${this.baseUrl}/get-metadata-by-reviewer`,
      {
        params,
      }
    );
  }

  /** Get assignment metadata for Uuid */
  getAssignmentMetaByUuid(
    assignmentUuid: string
  ): Observable<AssignmentMetadata> {
    const params = new HttpParams().set('assignmentUuid', assignmentUuid);

    return this.http.get<AssignmentMetadata>(
      `${this.baseUrl}/get-metadata-by-uuid`,
      {
        params,
      }
    );
  }

  /** Get Gerrit ChangeInfo list by Uuid */
  getGerritChangeInfoByUuid(assignmentUuid: string): Observable<ChangeInfo[]> {
    const params = new HttpParams().set('assignmentUuid', assignmentUuid);

    return this.http.get<ChangeInfo[]>(
      `${this.baseUrl}/get-commit-list-by-uuid`,
      {
        params,
      }
    );
  }

  /** Get ChangeDiff for */
  getChangeDiffs(
    assignmentUuid: string,
    changeId: string
  ): Observable<ChangeDiff[]> {
    const params = new HttpParams()
      .set('assignmentUuid', assignmentUuid)
      .set('changeId', changeId);

    return this.http.get<ChangeDiff[]>(
      `${this.baseUrl}/get-change-diff`,
      {
        params,
      }
    );
  }
}
