import { Injectable } from '@angular/core';
import { SPRING_URL_GITLAB } from '../service/constant.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GitlabCommitInfo } from '../interface/gitlab/gitlab-commit-info';

@Injectable({
  providedIn: 'root',
})
export class GitlabService {
  private baseUrl = SPRING_URL_GITLAB;

  constructor(private http: HttpClient) {}

  getRepoCommits(repoUrl: string): Observable<GitlabCommitInfo[]> {
    return this.http.get<GitlabCommitInfo[]>(
      `${this.baseUrl}/get-repo-commits?url=${repoUrl}`
    );
  }
}
