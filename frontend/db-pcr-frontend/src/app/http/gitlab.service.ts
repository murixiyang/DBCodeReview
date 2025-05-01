import { Injectable } from '@angular/core';
import { SPRING_URL_GITLAB } from '../service/constant.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CommitSchema, ProjectSchema } from '@gitbeaker/rest';

@Injectable({
  providedIn: 'root',
})
export class GitlabService {
  private baseUrl = SPRING_URL_GITLAB;

  constructor(private http: HttpClient) {}

  /**  Get user's project list */
  getProjects(): Observable<ProjectSchema[]> {
    return this.http.get<ProjectSchema[]>(`${this.baseUrl}/projects`, {
      withCredentials: true,
    });
  }

  /** Get project commits */
  getProjectCommits(projectId: number): Observable<CommitSchema[]> {
    return this.http.get<CommitSchema[]>(
      `${this.baseUrl}/get-project-commits?projectId=${projectId}`,
      { withCredentials: true }
    );
  }
}
