import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { SPRING_URL_REVIEW } from '../service/constant.service';
import { Observable } from 'rxjs';
import { ProjectDto } from '../interface/database/project-dto';
import { ChangeRequestDto } from '../interface/database/change-request-dto';
import { ReviewAssignmentPseudonymDto } from '../interface/database/review-assignment-dto';
import { GerritCommentInfo } from '../interface/gerrit/gerrit-comment-info';
import { GerritCommentInput } from '../interface/gerrit/gerrit-comment-input';
import { PseudonymGitlabCommitDto } from '../interface/database/pseudonym-gitlab-commit-dto';
import { NameCommentInfo } from '../interface/gerrit/name-comment-info';

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
  getChangeRequestForAssignment(
    assignmentId: string
  ): Observable<ChangeRequestDto[]> {
    const params = new HttpParams().set('assignmentId', assignmentId);

    return this.http.get<ChangeRequestDto[]>(
      `${this.baseUrl}/get-review-project-commits`,
      {
        params,
      }
    );
  }

  /** Get Author Assignment Pseudunym Dto by assignment id, getting in from author side */
  getAuthorAssignmentPseudonymDtoList(
    personalProjectId: string
  ): Observable<ReviewAssignmentPseudonymDto[]> {
    const params = new HttpParams().set(
      'projectId',
      personalProjectId.toString()
    );

    return this.http.get<ReviewAssignmentPseudonymDto[]>(
      `${this.baseUrl}/get-author-assignment-pseudonym`,
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

  /** Get Author Pseudonym commit info by gerrit change id */
  getAuthorPseudonymCommit(
    gerritChangeId: string
  ): Observable<PseudonymGitlabCommitDto> {
    const params = new HttpParams().set('gerritChangeId', gerritChangeId);

    return this.http.get<PseudonymGitlabCommitDto>(
      `${this.baseUrl}/get-author-pseudonym-commit`,
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

  /** Get changed file names for a gerrit change */
  getChangedFileNames(gerritChangeId: string): Observable<string[]> {
    const params = new HttpParams().set('gerritChangeId', gerritChangeId);

    return this.http.get<string[]>(`${this.baseUrl}/get-changed-files`, {
      params,
    });
  }

  /** Get changed file contents for a gerrit change */
  getChangedFileContents(
    gerritChangeId: string
  ): Observable<Map<string, string[]>> {
    const params = new HttpParams().set('gerritChangeId', gerritChangeId);

    return this.http.get<Map<string, string[]>>(
      `${this.baseUrl}/get-changed-files-content`,
      {
        params,
      }
    );
  }

  /** Get ChangeDiff for a Gerrit Change */
  getChangeDiffs(gerritChangeId: string): Observable<string> {
    const params = new HttpParams().set('gerritChangeId', gerritChangeId);

    return this.http.get(`${this.baseUrl}/get-change-diff`, {
      params,
      responseType: 'text',
    });
  }

  /** Get Existed comment on a Gerrit Change */
  getExistedComments(gerritChangeId: string): Observable<GerritCommentInfo[]> {
    const params = new HttpParams().set('gerritChangeId', gerritChangeId);

    return this.http.get<GerritCommentInfo[]>(
      `${this.baseUrl}/get-gerrit-change-comments`,
      {
        params,
      }
    );
  }

  /** Get Existed comments with Pseudonym attached */
  getExistedCommentsWithPseudonym(
    gerritChangeId: string
  ): Observable<NameCommentInfo[]> {
    const params = new HttpParams().set('gerritChangeId', gerritChangeId);

    return this.http.get<NameCommentInfo[]>(
      `${this.baseUrl}/get-gerrit-change-comments-with-pseudonym`,
      {
        params,
      }
    );
  }

  /** Get Existed comments with Username attached */
  getExistedCommentsWithUsername(
    gerritChangeId: string
  ): Observable<NameCommentInfo[]> {
    const params = new HttpParams().set('gerritChangeId', gerritChangeId);

    return this.http.get<NameCommentInfo[]>(
      `${this.baseUrl}/get-gerrit-change-comments-with-username`,
      {
        params,
      }
    );
  }

  /** Get Existed draft comment on a Gerrit Change for current user */
  getUserDraftComments(
    gerritChangeId: string
  ): Observable<GerritCommentInfo[]> {
    const params = new HttpParams().set('gerritChangeId', gerritChangeId);

    return this.http.get<GerritCommentInfo[]>(
      `${this.baseUrl}/get-user-gerrit-change-draft-comments`,
      {
        params,
      }
    );
  }

  /** Post a reviewer draft comment on a Gerrit Change */
  postReviewerDraftComment(
    gerritChangeId: string,
    assignmentId: string,
    commentInput: GerritCommentInput
  ): Observable<GerritCommentInfo> {
    const params = new HttpParams()
      .set('gerritChangeId', gerritChangeId)
      .set('assignmentId', assignmentId);
    return this.http.post<GerritCommentInfo>(
      `${this.baseUrl}/post-reviewer-gerrit-draft-comment`,
      commentInput,
      {
        params,
      }
    );
  }

  /** Post a author draft comment on a Gerrit Change */
  postAuthorDraftComment(
    gerritChangeId: string,
    assignmentId: string,
    commentInput: GerritCommentInput
  ): Observable<GerritCommentInfo> {
    const params = new HttpParams()
      .set('gerritChangeId', gerritChangeId)
      .set('assignmentId', assignmentId);
    return this.http.post<GerritCommentInfo>(
      `${this.baseUrl}/post-author-gerrit-draft-comment`,
      commentInput,
      {
        params,
      }
    );
  }

  /** Update a draft comment on a Gerrit Change */
  updateDraftComment(
    gerritChangeId: string,
    commentInput: GerritCommentInput
  ): Observable<GerritCommentInfo> {
    const params = new HttpParams().set('gerritChangeId', gerritChangeId);
    return this.http.put<GerritCommentInfo>(
      `${this.baseUrl}/update-gerrit-draft-comment`,
      commentInput,
      { params }
    );
  }

  /** Delete a draft comment on a Gerrit Change */
  deleteDraftComment(
    gerritChangeId: string,
    assignmentId: string,
    commentInput: GerritCommentInput
  ): Observable<void> {
    const params = new HttpParams()
      .set('gerritChangeId', gerritChangeId)
      .set('assignmentId', assignmentId);
    // HttpClient.delete() doesn’t accept a body, so we use the generic request() form:
    return this.http.request<void>(
      'DELETE',
      `${this.baseUrl}/delete-gerrit-draft-comment`,
      {
        params,
        body: commentInput,
      }
    );
  }

  /** Publish reviewer draft comments as a review */
  publishReviewerDraftComments(
    gerritChangeId: string,
    assignmentId: string,
    needResolve: boolean,
    draftInputs: GerritCommentInput[]
  ): Observable<void> {
    const params = new HttpParams()
      .set('gerritChangeId', gerritChangeId)
      .set('assignmentId', assignmentId)
      .set('needResolve', needResolve.toString());

    return this.http.post<void>(
      `${this.baseUrl}/publish-reviewer-gerrit-draft-comments`,
      draftInputs,
      {
        params,
      }
    );
  }

  /** Publish author draft comments as a review */
  publishAuthorDraftComments(
    gerritChangeId: string,
    assignmentId: string,
    draftInputs: GerritCommentInput[]
  ): Observable<void> {
    const params = new HttpParams()
      .set('gerritChangeId', gerritChangeId)
      .set('assignmentId', assignmentId);

    return this.http.post<void>(
      `${this.baseUrl}/publish-author-gerrit-draft-comments`,
      draftInputs,
      {
        params,
      }
    );
  }
}
