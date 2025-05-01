import { Injectable } from '@angular/core';
import { SPRING_URL_GITLAB } from '../service/constant.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GitlabCommitInfo } from '../interface/gitlab/gitlab-commit-info';
import { ProjectSchema } from '@gitbeaker/rest';;

@Injectable({
  providedIn: 'root',
})
export class GitlabService {
  private baseUrl = SPRING_URL_GITLAB;

  constructor(private http: HttpClient) {}

  /**  Get user's project list */
  getProjects(): Observable<ProjectSchema[]> {
    return this.http.get<ProjectSchema[]>(`${this.baseUrl}/projects`, {
      withCredentials: true, // send the session cookie to Spring
    });
  }

  getRepoCommits(repoUrl: string): Observable<GitlabCommitInfo[]> {
    return this.http.get<GitlabCommitInfo[]>(
      `${this.baseUrl}/get-repo-commits?url=${repoUrl}`
    );
  }
}
