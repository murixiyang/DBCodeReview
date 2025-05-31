import { Injectable } from '@angular/core';
import { SPRING_URL_EVALUATION } from '../service/constant.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FilePayload } from '../interface/eval/file-paylod';
import { EvalReviewDto } from '../interface/eval/eval-review-dto';
import { NamedAuthorCodeDto } from '../interface/eval/named-author-code-dto';
import { GerritCommentInput } from '../interface/gerrit/gerrit-comment-input';
import { GerritCommentInfo } from '../interface/gerrit/gerrit-comment-info';
import { ReactState } from '../interface/react-state';

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

  /** ------ COMMENTING -------- */

  /** Post a reviewer draft comment on a Gerrit Change */
  postDraftComment(
    gerritChangeId: string,
    commentInput: GerritCommentInput
  ): Observable<GerritCommentInfo> {
    const params = new HttpParams().set('gerritChangeId', gerritChangeId);
    return this.http.post<GerritCommentInfo>(
      `${this.baseUrl}/post-reviewer-gerrit-draft-comment`,
      commentInput,
      {
        params,
      }
    );
  }

  /* Get published Comments with no name */
  getExistedComments(gerritChangeId: string): Observable<GerritCommentInfo[]> {
    const params = new HttpParams().set('gerritChangeId', gerritChangeId);

    return this.http.get<GerritCommentInfo[]>(
      `${this.baseUrl}/get-gerrit-change-comments`,
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

  /** Publish reviewer draft comments as a review */
  publishDraftComments(
    gerritChangeId: string,
    draftInputs: GerritCommentInput[]
  ): Observable<void> {
    const params = new HttpParams().set('gerritChangeId', gerritChangeId);

    return this.http.post<void>(
      `${this.baseUrl}/publish-reviewer-gerrit-draft-comments`,
      draftInputs,
      {
        params,
      }
    );
  }

  /** Post thumb up or thumb down or cancel thumb for a comment */

  postThumbStateForComment(
    gerritChangeId: string,
    gerritCommentId: string,
    thumbState: ReactState
  ): Observable<void> {
    const params = new HttpParams()
      .set('gerritChangeId', gerritChangeId)
      .set('gerritCommentId', gerritCommentId)
      .set('thumbState', thumbState.toString().toUpperCase());

    return this.http.post<void>(
      `${this.baseUrl}/post-thumb-state-for-comment`,
      null,
      {
        params,
      }
    );
  }

  /** Delete a draft comment on a Gerrit Change */
  deleteDraftComment(
    gerritChangeId: string,
    commentInput: GerritCommentInput
  ): Observable<void> {
    const params = new HttpParams().set('gerritChangeId', gerritChangeId);
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
}
