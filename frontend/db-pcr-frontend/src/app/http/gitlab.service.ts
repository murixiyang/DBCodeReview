import { Injectable } from '@angular/core';
import { SPRING_URL_GITLAB } from '../service/constant.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CommitDiffSchema, CommitSchema, ProjectSchema } from '@gitbeaker/rest';
import { ProjectDto } from '../interface/database/project-dto';
import { GitlabCommitDto } from '../interface/database/gitlab-commit-dto';
import { CommitWithStatusDto } from '../interface/database/commit-with-status-dto';

@Injectable({
  providedIn: 'root',
})
export class GitlabService {
  private baseUrl = SPRING_URL_GITLAB;

  constructor(private http: HttpClient) {}

  /**  Get user's project list */
  getProjects(): Observable<ProjectDto[]> {
    return this.http.get<ProjectDto[]>(`${this.baseUrl}/projects`, {
      withCredentials: true,
    });
  }

  /** Get project by group */
  getGroupProjects(): Observable<ProjectDto[]> {
    return this.http.get<ProjectDto[]>(`${this.baseUrl}/group-projects`, {
      withCredentials: true,
    });
  }

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
}
