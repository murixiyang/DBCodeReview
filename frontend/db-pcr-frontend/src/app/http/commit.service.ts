import { Injectable } from '@angular/core';
import { SPRING_URL } from '../service/constant.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CommitWithStatusDto } from '../interface/database/commit-with-status-dto';
import { CommitDiffSchema } from '@gitbeaker/rest';

@Injectable({
  providedIn: 'root',
})
export class CommitService {
  private baseUrl = SPRING_URL;

  constructor(private http: HttpClient) {}

  /** Get project commits with CommitStatus */
  getCommitsWithStatus(projectId: string): Observable<CommitWithStatusDto[]> {
    return this.http.get<CommitWithStatusDto[]>(
      `${this.baseUrl}/get-commits-with-status?projectId=${projectId}`,
      { withCredentials: true }
    );
  }

  /** Get the list of modified files for a commit */
  getCommitDiff(
    projectId: string,
    sha: string
  ): Observable<CommitDiffSchema[]> {
    return this.http.get<any>(
      `${this.baseUrl}/get-commit-diff?projectId=${projectId}&sha=${sha}`,
      { withCredentials: true }
    );
  }

  /** Get gerrit change id related to a commit submission */
  getGerritChangeIdByCommitId(commitId: string): Observable<string> {
    return this.http.get(
      `${this.baseUrl}/get-gerrit-change-id?commitId=${commitId}`,
      {
        responseType: 'text',
        withCredentials: true,
      }
    );
  }
}
