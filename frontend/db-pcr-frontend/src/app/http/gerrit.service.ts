import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SPRING_URL } from '../service/constant.service';
import { ChangeInfo } from '../interface/gerrit/change-info';
import { ProjectInfoModel } from '../interface/gerrit/project-info';
import { ModiFileInfo } from '../interface/gerrit/modi-file-info';
import { DiffInfo } from '../interface/gerrit/diff-info';
import { CommentInput } from '../interface/gerrit/comment-input';
import { CommentInfo } from '../interface/gerrit/comment-info';

@Injectable({
  providedIn: 'root',
})
export class GerritService {
  private baseUrl = SPRING_URL;

  constructor(private http: HttpClient) {}

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

  getProjectList(): Observable<Map<string, ProjectInfoModel>> {
    return this.http.get<Map<string, ProjectInfoModel>>(
      `${this.baseUrl}/get-project-list`
    );
  }

  getChangesOfProject(projectId: string): Observable<ChangeInfo[]> {
    return this.http.get<ChangeInfo[]>(
      `${this.baseUrl}/get-changes?q=project:${projectId}`
    );
  }

  getModifiedFileInChange(
    changeId: string,
    revisionId: string
  ): Observable<Map<string, ModiFileInfo>> {
    return this.http.get<Map<string, ModiFileInfo>>(
      `${this.baseUrl}/get-modified-file-list?changeId=${changeId}&revisionId=${revisionId}`
    );
  }

  getFileDiff(
    changeId: string,
    revisionId: string,
    filePath: string
  ): Observable<DiffInfo> {
    return this.http.get<DiffInfo>(
      `${this.baseUrl}/get-file-diff?changeId=${changeId}&revisionId=${revisionId}&filePath=${filePath}`
    );
  }

  getAllComments(
    changeId: string,
    revisionId: string
  ): Observable<Map<string, CommentInfo[]>> {
    return this.http.get<Map<string, CommentInfo[]>>(
      `${this.baseUrl}/get-comments?changeId=${changeId}&revisionId=${revisionId}`
    );
  }

  getAllDraftComments(
    changeId: string,
    revisionId: string
  ): Observable<Map<string, CommentInfo[]>> {
    return this.http.get<Map<string, CommentInfo[]>>(
      `${this.baseUrl}/get-draft-comments?changeId=${changeId}&revisionId=${revisionId}`
    );
  }

  putDraftComment(
    changeId: string,
    revisionId: string,
    commentInput: CommentInput
  ): Observable<CommentInfo> {
    const url = `${this.baseUrl}/put-draft-comment?changeId=${changeId}&revisionId=${revisionId}`;
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.put<CommentInfo>(url, commentInput, { headers });
  }

  updateDraftComment(
    changeId: string,
    revisionId: string,
    commentInput: CommentInput
  ): Observable<CommentInfo> {
    const url = `${this.baseUrl}/update-draft-comment?changeId=${changeId}&revisionId=${revisionId}&draftId=${commentInput.id}`;
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.put<CommentInfo>(url, commentInput, { headers });
  }

  deleteDraftComment(
    changeId: string,
    revisionId: string,
    draftId: string
  ): Observable<any> {
    const url = `${this.baseUrl}/delete-draft-comment?changeId=${changeId}&revisionId=${revisionId}&draftId=${draftId}`;
    return this.http.delete(url);
  }
}
